/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import com.google.common.collect.ImmutableList
import com.google.common.collect.Sets
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import org.jooq.lambda.fi.lang.CheckedRunnable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.ArrayDeque
import java.util.Optional
import java.util.Queue
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

/**
 * Represents a simulation engine that manages execution and scheduling.
 * Provides multiple factory methods to simplify the creation process.
 *
 * @param T the concentration type
 * @param P the position type, extending [Position]
 * @property environment the simulation environment
 * @property scheduler the scheduler managing event execution
 */
open class Engine<T, P : Position<out P>>(
    private val environment: Environment<T, P>,
    protected val scheduler: Scheduler<T>,
) : Simulation<T, P> {

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
    private val commands: BlockingQueue<CheckedRunnable> = LinkedBlockingQueue()

    /** Queue of updates to be processed after execution. */
    protected val afterExecutionUpdates: Queue<Update> = ArrayDeque()

    /** Manages dependencies between reactions in the simulation. */
    protected val dependencyGraph: DependencyGraph<T>

    /** List of registered output monitors for simulation events. */
    protected val monitors: MutableList<OutputMonitor<T, P>> = CopyOnWriteArrayList()

    /**
     * Constructs a simulation with a default scheduler.
     *
     * This constructor initializes the simulation with a default [ArrayIndexedPriorityQueue].
     * If you need a custom [DependencyGraph] or [Scheduler], use the other constructor.
     *
     * @param environment the simulation environment
     */
    constructor(environment: Environment<T, P>) : this(environment, ArrayIndexedPriorityQueue())

    /**
     * Constructs a simulation with a custom scheduler.
     *
     * This constructor allows specifying a custom [Scheduler] implementation.
     * If a custom [DependencyGraph] is also needed, it should be provided separately.
     *
     * @param environment the simulation environment
     * @param scheduler the scheduler responsible for event execution
     */
    init {
        LOGGER.trace("Engine created")
        environment.simulation = this
        dependencyGraph = JGraphTDependencyGraph(environment)
    }

    /**
     * Adds an output monitor to track simulation events.
     *
     * @param op the [OutputMonitor] to add
     */
    override fun addOutputMonitor(op: OutputMonitor<T, P>) {
        monitors.add(op)
    }

    /** Ensures that the method is called from the simulation thread. */
    private fun checkCaller() {
        check(this::class.java == BatchEngine::class.java || Thread.currentThread() == simulationThread) {
            "This method must be called from the simulation thread."
        }
    }

    /**
     * Executes an action while holding the status lock.
     *
     * @param action the action to execute
     * @return the result of the action
     */
    private fun <R> doOnStatus(action: () -> R): R = statusLock.run {
        lock()
        try {
            action()
        } finally {
            unlock()
        }
    }

    /**
     * Performs a single step of the simulation.
     */
    protected open fun doStep() {
        val nextEvent = scheduler.getNext() ?: run {
            newStatus(Status.TERMINATED)
            LOGGER.info("No more reactions.")
            return
        }
        val scheduledTime = nextEvent.tau
        check(scheduledTime >= time) {
            "$nextEvent is scheduled in the past at time $scheduledTime. Current time: $time; current step: $step."
        }
        currentTime = scheduledTime
        if (scheduledTime.isFinite && nextEvent.canExecute()) {
            nextEvent.conditions.forEach { it.reactionReady() }
            nextEvent.execute()
            var toUpdate: Set<Actionable<T>> = dependencyGraph.outboundDependencies(nextEvent)
            if (afterExecutionUpdates.isNotEmpty()) {
                afterExecutionUpdates.forEach { it.performChanges() }
                afterExecutionUpdates.clear()
                toUpdate = Sets.union(toUpdate, dependencyGraph.outboundDependencies(nextEvent))
            }
            toUpdate.forEach { updateReaction(it) }
        }
        nextEvent.update(time, true, environment)
        scheduler.updateReaction(nextEvent)
        monitors.forEach { it.stepDone(environment, nextEvent, time, step) }
        if (environment.isTerminated) {
            newStatus(Status.TERMINATED)
            LOGGER.info("Termination condition reached.")
        }
        currentStep = step + 1
    }

    /** @return the simulation environment. */
    override fun getEnvironment(): Environment<T, P> = environment

    /** @return the last error encountered, if any. */
    override fun getError(): Optional<Throwable> = error

    /** @return the current simulation status. */
    override fun getStatus(): Status = status

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

    /**
     * Registers a newly added neighbor.
     *
     * @param node the reference node
     * @param n the neighbor node
     */
    override fun neighborAdded(node: Node<T>, n: Node<T>) {
        checkCaller()
        afterExecutionUpdates.add(NeighborAdded(node, n))
    }

    /**
     * Registers a removed neighbor.
     *
     * @param node the reference node
     * @param n the removed neighbor node
     */
    override fun neighborRemoved(node: Node<T>, n: Node<T>) {
        checkCaller()
        afterExecutionUpdates.add(NeighborRemoved(node, n))
    }

    /**
     * Updates the simulation status.
     *
     * @param next the new status to set
     * @return a future that completes when the status is updated
     */
    protected open fun newStatus(next: Status): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        schedule(
            CheckedRunnable {
                doOnStatus {
                    if (next.isReachableFrom(status)) {
                        status = next
                        lockForStatus(next).releaseAll()
                    }
                    future.complete(null)
                }
            },
        )
        return future
    }

    /**
     * Handles the addition of a new node.
     *
     * @param node the newly added node
     */
    override fun nodeAdded(node: Node<T>) {
        checkCaller()
        afterExecutionUpdates.add(NodeAddition(node))
    }

    /**
     * Handles node movement.
     *
     * @param node the moved node
     */
    override fun nodeMoved(node: Node<T>) {
        checkCaller()
        afterExecutionUpdates.add(Movement(node))
    }

    /**
     * Handles the removal of a node.
     *
     * @param node the removed node
     * @param oldNeighborhood the node's neighborhood before removal (used for reverse dependencies)
     */
    override fun nodeRemoved(node: Node<T>, oldNeighborhood: Neighborhood<T>) {
        checkCaller()
        afterExecutionUpdates.add(NodeRemoval(node))
    }

    /** Pauses the simulation. */
    override fun pause(): CompletableFuture<Unit> = newStatus(Status.PAUSED)

    /** Resumes the simulation. */
    override fun play(): CompletableFuture<Unit> = newStatus(Status.RUNNING)

    /**
     * Registers a newly added reaction.
     *
     * @param reactionToAdd the reaction to add
     */
    override fun reactionAdded(reactionToAdd: Actionable<T>) {
        reactionChanged(ReactionAddition(reactionToAdd))
    }

    /**
     * Registers a removed reaction.
     *
     * @param reactionToRemove the reaction to remove
     */
    override fun reactionRemoved(reactionToRemove: Actionable<T>) {
        reactionChanged(ReactionRemoval(reactionToRemove))
    }

    /**
     * Handles reaction changes.
     *
     * @param update the update describing the reaction change
     */
    private fun reactionChanged(update: AbstractUpdateOnReaction) {
        checkCaller()
        afterExecutionUpdates.add(update)
    }

    /** Retrieves the reactions that require updates after execution. */
    private fun reactionsToUpdateAfterExecution(): Sequence<Actionable<T>> =
        afterExecutionUpdates.asSequence().flatMap { it.reactionsToUpdate }.distinct()

    private fun processCommand(command: CheckedRunnable) {
        command.run()
        val updated = mutableSetOf<Actionable<T>>()
        reactionsToUpdateAfterExecution().forEach {
            updated.add(it)
            updateReaction(it)
        }
        afterExecutionUpdates.forEach { it.performChanges() }
        afterExecutionUpdates.clear()
        reactionsToUpdateAfterExecution().forEach { if (it !in updated) updateReaction(it) }
    }

    /**
     * @param op the OutputMonitor to add
     */
    override fun removeOutputMonitor(op: OutputMonitor<T, P>) {
        monitors.remove(op)
    }

    private fun processCommandsWhileIn(status: Status) {
        while (this.status == status) {
            processCommand(commands.take())
        }
    }

    /**
     * Runs the simulation.
     */
    @Suppress("TooGenericExceptionCaught")
    override fun run() {
        synchronized(environment) {
            try {
                LOGGER.info("Starting engine {} with scheduler {}", javaClass, scheduler.javaClass)
                simulationThread = Thread.currentThread()
                environment.globalReactions.forEach { reactionAdded(it) }
                environment.forEach { node -> node.reactions.forEach { scheduleReaction(it) } }
                status = Status.READY
                LOGGER.trace("Thread {} started running.", Thread.currentThread().id)
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
            } catch (e: Throwable) { // NOPMD: forced by CheckedRunnable
                error = Optional.of(e)
                LOGGER.error("The simulation engine crashed.", e)
            } finally {
                status = Status.TERMINATED
                commands.clear()
                try {
                    monitors.forEach { it.finished(environment, time, step) }
                } catch (e: Throwable) { // NOPMD: we need to catch everything
                    error.ifPresentOrElse({ it.addSuppressed(e) }, { error = Optional.of(e) })
                }
                afterRun()
            }
        }
    }

    /** Override this to execute custom logic after the simulation run. */
    protected fun afterRun() = Unit

    /**
     * Pauses the simulation when the given condition is met.
     *
     * @param condition the condition to trigger the pause
     * @return a [CompletableFuture] that completes when the simulation is paused
     */
    private fun pauseWhen(condition: BooleanSupplier): CompletableFuture<Unit> {
        val future = CompletableFuture<Unit>()
        addOutputMonitor(object : OutputMonitor<T, P> {
            override fun initialized(initializedEnvironment: Environment<T, P>) {
                if (condition.asBoolean) {
                    monitors.remove(this)
                    pause().thenRun { future.complete(null) }
                }
            }
            override fun stepDone(
                targetEnvironment: Environment<T, P>,
                reaction: Actionable<T>?,
                time: Time,
                step: Long,
            ) {
                initialized(targetEnvironment)
            }
        })
        return future
    }

    /**
     * Schedules a task for execution.
     *
     * @param runnable the task to execute
     */
    override fun schedule(runnable: CheckedRunnable) {
        check(status != Status.TERMINATED) { "This simulation is terminated and cannot be resumed." }
        commands.add(runnable)
    }

    /**
     * Schedules a reaction by setting up dependencies and adding it to the scheduler.
     *
     * @param reaction the reaction to schedule
     */
    private fun scheduleReaction(reaction: Actionable<T>) {
        dependencyGraph.createDependencies(reaction)
        reaction.initializationComplete(time, environment)
        scheduler.addReaction(reaction)
    }

    /** Terminates the simulation. */
    override fun terminate(): CompletableFuture<Unit> = newStatus(Status.TERMINATED)

    /** @return a string representation of the engine. */
    override fun toString(): String = "${javaClass.simpleName} t: $time, s: $step"

    /**
     * Updates the given reaction, adjusting its scheduling if needed.
     *
     * @param r the reaction to update
     */
    protected fun updateReaction(r: Actionable<T>) {
        val previousTau = r.tau
        r.update(time, false, environment)
        if (r.tau != previousTau) scheduler.updateReaction(r)
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
     * Waits for the simulation to reach a given status.
     *
     * @param next the [Status] the simulation should reach before returning
     * @param timeout the maximum time to wait
     * @param tu the [TimeUnit] defining the timeout
     * @return the current simulation status
     */
    override fun waitFor(next: Status, timeout: Long, tu: TimeUnit): Status =
        lockForStatus(next).waitFor(next, timeout, tu)

    /** @return the list of registered output monitors. */
    override fun getOutputMonitors(): List<OutputMonitor<T, P>> = ImmutableList.copyOf(monitors)

    /**
     * Represents a simulation update operation.
     */
    protected open inner class Update {
        /** Performs the update. Override to implement specific behavior. */
        open fun performChanges() {}

        /** The reactions that require an update. */
        open val reactionsToUpdate: Sequence<Actionable<T>> = emptySequence()
    }

    /**
     * Handles node movement and updates affected reactions.
     *
     * @param sourceNode the node that moved
     */
    private inner class Movement(private val sourceNode: Node<T>) : Update() {
        override val reactionsToUpdate: Sequence<Actionable<T>>
            get() = getReactionsRelatedTo(sourceNode, environment.getNeighborhood(sourceNode)).filter {
                it.inboundDependencies.any { dependency -> dependency.dependsOn(Dependency.MOVEMENT) }
            }

        private fun getReactionsRelatedTo(source: Node<T>, neighborhood: Neighborhood<T>): Sequence<Actionable<T>> =
            sequenceOf(
                source.reactions.asSequence(),
                neighborhood.getNeighbors().asSequence()
                    .flatMap { it.reactions.asSequence() }
                    .filter { it.inputContext == Context.NEIGHBORHOOD },
                dependencyGraph.globalInputContextReactions().asSequence(),
            ).flatten()
    }

    /**
     * Applies an update to all reactions of a node.
     *
     * @param sourceNode the node whose reactions should be updated
     * @param reactionLevelOperation the update operation to apply to each reaction
     */
    private open inner class UpdateOnNode(
        private val sourceNode: Node<T>,
        private val reactionLevelOperation: (Reaction<T>) -> Update,
    ) : Update() {
        override fun performChanges() {
            sourceNode.reactions.map(reactionLevelOperation).forEach { it.performChanges() }
        }
    }

    private inner class NodeRemoval(sourceNode: Node<T>) : UpdateOnNode(sourceNode, { ReactionRemoval(it) })

    private inner class NodeAddition(sourceNode: Node<T>) : UpdateOnNode(sourceNode, { ReactionAddition(it) })

    /**
     * Represents an update affecting a specific reaction.
     *
     * @param sourceReaction the reaction affected by this update
     */
    private open inner class AbstractUpdateOnReaction(val sourceReaction: Actionable<T>) : Update() {
        override val reactionsToUpdate: Sequence<Actionable<T>> = sequenceOf(sourceReaction)
    }

    /** Handles the removal of a reaction. */
    private inner class ReactionRemoval(source: Actionable<T>) : AbstractUpdateOnReaction(source) {
        override fun performChanges() {
            dependencyGraph.removeDependencies(sourceReaction)
            scheduler.removeReaction(sourceReaction)
        }
    }

    /** Handles the addition of a reaction. */
    private inner class ReactionAddition(source: Actionable<T>) : AbstractUpdateOnReaction(source) {
        override fun performChanges() {
            this@Engine.scheduleReaction(sourceReaction)
        }
    }

    /**
     * Handles neighborhood changes, ensuring updates to relevant reactions.
     *
     * @param sourceNode the node initiating the change
     * @param targetNode the node affected by the change
     */
    private open inner class NeighborhoodChanged(val sourceNode: Node<T>, val targetNode: Node<T>) : Update() {
        override val reactionsToUpdate: Sequence<Actionable<T>>
            get() {
                val subjects = sequenceOf(sourceNode, targetNode)
                val sourceNeighbors = environment.getNeighborhood(sourceNode).asSequence()
                val targetNeighbors = environment.getNeighborhood(targetNode).asSequence()
                val allSubjects = (subjects + sourceNeighbors + targetNeighbors).distinct()
                return allSubjects.flatMap { it.reactions.asSequence() }.filter {
                    it.inputContext ==
                        Context.NEIGHBORHOOD
                } +
                    dependencyGraph.globalInputContextReactions().asSequence()
            }
    }

    /** Handles the addition of a neighbor. */
    private inner class NeighborAdded(source: Node<T>, target: Node<T>) : NeighborhoodChanged(source, target) {
        override fun performChanges() {
            dependencyGraph.addNeighbor(sourceNode, targetNode)
        }
    }

    /** Handles the removal of a neighbor. */
    private inner class NeighborRemoved(source: Node<T>, target: Node<T>) : NeighborhoodChanged(source, target) {
        override fun performChanges() {
            dependencyGraph.removeNeighbor(sourceNode, targetNode)
        }
    }

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

    private companion object {
        /** Logger instance. */
        val LOGGER: Logger = LoggerFactory.getLogger(Engine::class.java)
    }
}
