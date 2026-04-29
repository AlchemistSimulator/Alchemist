/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.core

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import java.util.Collections
import java.util.Optional
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
import org.jooq.lambda.fi.lang.CheckedRunnable
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
abstract class AbstractEngine<T, P : Position<out P>>(private val environment: Environment<T, P>) : Simulation<T, P> {

    private val statusLock: Lock = ReentrantLock()

    /** Lock for synchronizing access to the simulation status. */
    @Volatile private var status: Status = Status.INIT

    /** Tracks errors that occurred during simulation execution. */
    private var error: Optional<Throwable> = Optional.empty()

    /** Current simulation time. */
    protected var currentTime: Time = Time.ZERO
        @Synchronized get

        @Synchronized set

    /** Current simulation step count. */
    protected var currentStep: Long = 0
        @Synchronized get

        @Synchronized set

    /** Thread executing the simulation. */
    private var simulationThread: Thread? = null

    /** Locks associated with each simulation status. */
    protected val statusLocks: Map<Status, SynchBox> = Status.entries.associateWith { SynchBox() }

    /** Queue of scheduled simulation commands. */
    protected val commands: BlockingQueue<CheckedRunnable> = LinkedBlockingQueue()

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
     * @param op the OutputMonitor to remove
     */
    override fun removeOutputMonitor(op: OutputMonitor<T, P>) {
        monitors.remove(op)
    }

    /** @return the list of registered output monitors. */
    override fun getOutputMonitors(): List<OutputMonitor<T, P>> = Collections.unmodifiableList(monitors)

    /** @return the simulation environment. */
    override fun getEnvironment(): Environment<T, P> = environment

    /** @return the last error encountered, if any. */
    override fun getError(): Optional<Throwable> = error

    protected fun setError(error: Optional<Throwable>) {
        this.error = error
    }

    /** @return the current simulation status. */
    override fun getStatus(): Status = status

    protected fun setStatus(status: Status) {
        this.status = status
    }

    /** @return the current step (thread-safe). */
    @Synchronized override fun getStep(): Long = currentStep

    /** @return the current simulation time (thread-safe). */
    @Synchronized override fun getTime(): Time = currentTime

    /**
     * Moves the simulation forward until the given step is reached.
     *
     * @param step the target step to execute up to.
     * @return a [CompletableFuture] that completes once the step is reached.
     */
    override fun goToStep(step: Long): CompletableFuture<Unit> = pauseWhen { getStep() >= step }

    /**
     * Moves the simulation forward until the given time is reached.
     *
     * @param t the target simulation time.
     * @return a [CompletableFuture] that completes once the time is reached.
     */
    override fun goToTime(t: Time): CompletableFuture<Unit> = pauseWhen { time >= t }

    /** Pauses the simulation. */
    override fun pause(): CompletableFuture<Unit> = newStatus(Status.PAUSED)

    /** Resumes the simulation. */
    override fun play(): CompletableFuture<Unit> = newStatus(Status.RUNNING)

    /** Terminates the simulation. */
    override fun terminate(): CompletableFuture<Unit> = newStatus(Status.TERMINATED)

    /**
     * Waits for the simulation to reach a given status.
     *
     * @param next the [Status] the simulation should reach before returning
     * @param timeout the maximum time to wait
     * @param tu the [TimeUnit] defining the timeout
     * @return the current simulation status
     */
    override fun waitFor(next: Status, timeout: Long, tu: TimeUnit): Status =
        lockForStatus(next).waitFor(next, timeout, tu)

    /**
     * Schedules a task for execution.
     *
     * @param runnable the task to execute
     */
    override fun schedule(runnable: CheckedRunnable) {
        check(status != Status.TERMINATED) { "This simulation is terminated and cannot be resumed." }
        commands.add(runnable)
    }

    /** Ensures that the method is called from the simulation thread. */
    protected fun checkCaller() {
        check(Thread.currentThread() == simulationThread) {
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
    protected open fun newStatus(next: Status): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        schedule {
            doOnStatus {
                if (next.isReachableFrom(status)) {
                    status = next
                    lockForStatus(next).releaseAll()
                }
                future.complete(null)
            }
        }
        return future
    }

    protected fun processCommandsWhileIn(status: Status) {
        while (this.status == status) {
            processCommand(commands.take())
        }
    }

    protected open fun processCommand(command: CheckedRunnable) {
        command.run()
    }

    /**
     * Retrieves the synchronization lock for a given status.
     *
     * @param futureStatus the status to obtain a lock for
     * @return the corresponding synchronization lock
     * @throws IllegalStateException if the requested status lock does not exist
     */
    private fun lockForStatus(futureStatus: Status): SynchBox = checkNotNull(statusLocks[futureStatus]) {
        "Inconsistent state: the Alchemist engine tried to synchronize on a non-existing lock. " +
            "Searching for status: $futureStatus, available locks: $statusLocks"
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

            override fun initialized(initializedEnvironment: Environment<T, P>) {
                checkConditionAndPause()
            }

            override fun stepDone(
                targetEnvironment: Environment<T, P>,
                reaction: Actionable<T>?,
                time: Time,
                step: Long,
            ) {
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
    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        synchronized(environment) {
            try {
                val logger = LoggerFactory.getLogger(this::class.java)
                logger.info("Starting engine {} with scheduler", this::class.java.simpleName)
                simulationThread = Thread.currentThread()

                initialize()

                status = Status.READY
                logger.trace("Thread {} started running.", Thread.currentThread().id)
                monitors.forEach { it.initialized(environment) }
                processCommandsWhileIn(Status.READY)
                while (status != Status.TERMINATED && time < Time.INFINITY) {
                    while (commands.isNotEmpty()) {
                        processCommand(commands.poll())
                    }
                    if (status == Status.RUNNING) {
                        doStep()
                    }
                    processCommandsWhileIn(Status.PAUSED)
                }
            } catch (e: Throwable) {
                error = Optional.of(e)
                LoggerFactory.getLogger(this::class.java).error("The simulation engine crashed.", e)
            } finally {
                status = Status.TERMINATED
                commands.clear()
                try {
                    monitors.forEach { it.finished(environment, time, step) }
                } catch (e: Throwable) {
                    error.ifPresentOrElse({ it.addSuppressed(e) }, { error = Optional.of(e) })
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
    override fun toString(): String = "${javaClass.simpleName} t: $time, s: $step"

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
        fun waitFor(next: Status, timeout: Long, tu: TimeUnit): Status = doOnStatus {
            var notTimedOut = true
            while (notTimedOut && next != status && next.isReachableFrom(status)) {
                try {
                    queueLength.incrementAndGet()
                    notTimedOut = statusReached.await(timeout, tu)
                    queueLength.decrementAndGet()
                } catch (e: InterruptedException) {
                    LOGGER.info("Spurious wakeup?", e)
                }
            }
            if (queueLength.get() == 0) allReleased.signal()
            status
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
