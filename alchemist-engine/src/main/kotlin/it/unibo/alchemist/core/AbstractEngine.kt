/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import arrow.core.None
import arrow.core.Option
import arrow.core.some
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.observables.util.MutableObservables.observeShareable
import it.unibo.alchemist.model.observation.MutableObservable
import java.util.Collections
import java.util.concurrent.BlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import java.util.function.BooleanSupplier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Abstract base class for simulation engines, providing common functionality for
 * status management, time tracking, command scheduling, and output monitoring.
 *
 * @param T the concentration type
 * @param P the position type
 * @property environment the simulation environment
 */
abstract class AbstractEngine<T, P : Position<out P>>(final override val environment: Environment<T, P>) :
    Simulation<T, P> {

    private val statusLock: Lock = ReentrantLock()

    /** The current simulation status. */
    @Volatile
    final override var state: SimulationState = SimulationState.INIT
        protected set

    /** The error that made the simulation fail, if any. */
    final override var error: Option<Throwable> = None
        protected set

    /** The registered output monitors, as a read-only view. */
    final override val outputMonitors: List<OutputMonitor<T, P>> get() = Collections.unmodifiableList(monitors)

    /** The current step (thread-safe). */
    final override val step: Long get() = currentStep

    /** The simulation time. */
    // Shareable, so that the current time can be read from any thread.
    final override val time: MutableObservable<Time> = observeShareable(Time.ZERO)

    /** The current simulation time (thread-safe). */
    final override val currentTime: Time get() = time.current

    /** Current simulation step count. */
    protected var currentStep: Long = 0
        @Synchronized get

        @Synchronized set

    /** Thread executing the simulation. */
    @Volatile
    private var simulationThread: Thread? = null

    /** Locks associated with each simulation status. */
    protected val statusLocks: Map<SimulationState, SynchBox> = SimulationState.entries.associateWith { SynchBox() }

    /** Queue of scheduled simulation commands. */
    protected val commands: BlockingQueue<() -> Unit> = LinkedBlockingQueue()

    /** List of registered output monitors for simulation events. */
    protected val monitors: MutableList<OutputMonitor<T, P>> = CopyOnWriteArrayList()

    init {
        LOGGER.trace("Engine created")
        environment.simulation = this
    }

    /**
     * Adds an output monitor to track simulation events.
     *
     * @param op the [OutputMonitor] to add
     */
    override fun addOutputMonitor(op: OutputMonitor<T, P>) {
        monitors.add(op)
    }

    /**
     * @param monitor the OutputMonitor to remove
     */
    override fun removeOutputMonitor(monitor: OutputMonitor<T, P>) {
        monitors.remove(monitor)
    }

    /** Records a failure while preserving an existing primary failure. */
    protected fun recordError(failure: Throwable) {
        error.onSome { it.addSuppressed(failure) }.onNone { error = failure.some() }
    }

    /**
     * Moves the simulation forward until the given step is reached.
     *
     * @param steps the target step to execute up to.
     * @return a [CompletableFuture] that completes once the step is reached.
     */
    override fun goToStep(steps: Long): CompletableFuture<Unit> = pauseWhen { step >= steps }

    /**
     * Moves the simulation forward until the given time is reached.
     *
     * @param time the target simulation time.
     * @return a [CompletableFuture] that completes once the time is reached.
     */
    override fun goToTime(time: Time): CompletableFuture<Unit> = pauseWhen { currentTime >= time }

    /** Pauses the simulation. */
    override fun pause(): CompletableFuture<Unit> = newStatus(SimulationState.PAUSED)

    /** Resumes the simulation. */
    override fun play(): CompletableFuture<Unit> = newStatus(SimulationState.RUNNING)

    /** Terminates the simulation. */
    override fun terminate(): CompletableFuture<Unit> = newStatus(SimulationState.TERMINATED)

    /**
     * Waits for the simulation to reach a given status.
     *
     * @param state the [SimulationState] the simulation should reach before returning
     * @param timeout the maximum time to wait
     * @param timeunit the [TimeUnit] defining the timeout
     * @return the current simulation status
     */
    override fun waitFor(state: SimulationState, timeout: Long, timeunit: TimeUnit): SimulationState =
        lockForStatus(state).waitFor(state, timeout, timeunit)

    /**
     * Schedules a task for execution.
     *
     * @param action the task to execute
     */
    override fun schedule(action: () -> Unit) {
        check(state != SimulationState.TERMINATED) { "This simulation is terminated and cannot be resumed." }
        commands.add(action)
    }

    /**
     * Ensures that the method is called from the simulation thread once [run] has established it.
     *
     * Before [run], direct setup by test harnesses and embedders remains supported.
     */
    protected fun checkCaller() {
        val owner = simulationThread
        check(owner == null || Thread.currentThread() == owner) {
            "This method must be called from the simulation thread."
        }
    }

    /**
     * Executes an action while holding the status lock.
     *
     * @param action the action to execute
     * @return the result of the action
     */
    protected fun <R> doOnStatus(action: () -> R): R = statusLock.run {
        lock()
        try {
            action()
        } finally {
            unlock()
        }
    }

    /**
     * Updates the simulation status.
     *
     * @param next the new status to set
     * @return a future that completes when the status is updated
     */
    protected open fun newStatus(next: SimulationState): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        schedule {
            doOnStatus {
                if (next.isReachableFrom(state)) {
                    state = next
                    lockForStatus(next).releaseAll()
                }
                future.complete(null)
            }
        }
        return future
    }

    protected fun processCommandsWhileIn(status: SimulationState) {
        while (this.state == status) {
            processCommand(commands.take())
        }
    }

    protected open fun processCommand(command: () -> Unit) {
        command()
    }

    /**
     * Retrieves the synchronization lock for a given status.
     *
     * @param futureSimulationState the status to obtain a lock for
     * @return the corresponding synchronization lock
     * @throws IllegalStateException if the requested status lock does not exist
     */
    private fun lockForStatus(futureSimulationState: SimulationState): SynchBox =
        checkNotNull(statusLocks[futureSimulationState]) {
            "Inconsistent state: the Alchemist engine tried to synchronize on a non-existing lock. " +
                "Searching for status: $futureSimulationState, available locks: $statusLocks"
        }

    /**
     * Pauses the simulation when the given condition is met.
     *
     * @param condition the condition to trigger the pause
     * @return a [CompletableFuture] that completes when the simulation is paused
     */
    private fun pauseWhen(condition: BooleanSupplier): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        val monitor = object : OutputMonitor<T, P> {
            @Volatile
            private var hasTriggered = false

            override fun initialized(environment: Environment<T, P>) {
                checkConditionAndPause()
            }

            override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
                checkConditionAndPause()
            }

            private fun checkConditionAndPause() {
                if (!hasTriggered && condition.asBoolean) {
                    hasTriggered = true
                    monitors.remove(this)
                    pause().thenRun { future.complete(null) }
                }
            }
        }
        addOutputMonitor(monitor)
        return future
    }

    /**
     * Internal method to run the simulation loop.
     * Implementations must provide [initialize], [doStep], and hooks for run cycle.
     */
    @Suppress("TooGenericExceptionCaught") // Catching Throwable to ensure the engine can handle all errors gracefully.
    override fun run() {
        synchronized(environment) {
            try {
                val logger = LoggerFactory.getLogger(this::class.java)
                logger.info("Starting engine {} with scheduler", this::class.java.simpleName)
                simulationThread = Thread.currentThread()

                initialize()

                state = SimulationState.READY
                logger.trace("Thread {} started running.", Thread.currentThread().threadId())
                monitors.forEach { it.initialized(environment) }
                processCommandsWhileIn(SimulationState.READY)
                while (state != SimulationState.TERMINATED && currentTime < Time.INFINITY) {
                    while (commands.isNotEmpty()) {
                        processCommand(commands.poll())
                    }
                    if (state == SimulationState.RUNNING) {
                        doStep()
                    }
                    processCommandsWhileIn(SimulationState.PAUSED)
                }
            } catch (e: Throwable) {
                error = e.some()
                LoggerFactory.getLogger(this::class.java).error("The simulation engine crashed.", e)
            } finally {
                state = SimulationState.TERMINATED
                commands.clear()
                try {
                    monitors.forEach { it.finished(environment, currentTime, step) }
                } catch (e: Throwable) {
                    recordError(e)
                }
                afterRun()
            }
        }
    }

    /**
     * Initializes the simulation engine.
     *
     * This method is called during the setup phase of the simulation engine and is responsible
     * for preparing all necessary internal states and resources required for the engine's execution,
     * e.g. initial scheduling of global and nodes' reactions.
     */
    protected abstract fun initialize()

    /**
     * Performs a single step of the simulation.
     */
    protected abstract fun doStep()

    /** Override this to execute custom logic after the simulation run. */
    protected open fun afterRun() = Unit

    /** @return a string representation of the engine. */
    override fun toString(): String = "${javaClass.simpleName} t: $currentTime, s: $step"

    /**
     * Synchronization helper for status transitions.
     */
    protected inner class SynchBox {
        private val queueLength = AtomicInteger()
        private val statusReached: Condition = statusLock.newCondition()
        private val allReleased: Condition = statusLock.newCondition()

        /**
         * Waits for the specified status until the timeout expires.
         *
         * @param next the target status
         * @param timeout the maximum time to wait
         * @param tu the time unit for the timeout
         * @return the current status after waiting
         */
        fun waitFor(next: SimulationState, timeout: Long, tu: TimeUnit): SimulationState = doOnStatus {
            var notTimedOut = true
            while (notTimedOut && next != state && next.isReachableFrom(state)) {
                try {
                    queueLength.incrementAndGet()
                    notTimedOut = statusReached.await(timeout, tu)
                    queueLength.decrementAndGet()
                } catch (e: InterruptedException) {
                    LOGGER.info("Spurious wakeup?", e)
                }
            }
            if (queueLength.get() == 0) allReleased.signal()
            state
        }

        /** Releases all locks. */
        fun releaseAll() {
            doOnStatus {
                while (queueLength.get() != 0) {
                    statusReached.signalAll()
                    allReleased.awaitUninterruptibly()
                }
            }
        }
    }

    protected companion object {
        /** Logger instance. */
        val LOGGER: Logger = LoggerFactory.getLogger(Engine::class.java)
    }
}
