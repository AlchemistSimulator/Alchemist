/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.core

import com.google.common.collect.Sets
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Dependency
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import java.util.ArrayDeque
import java.util.Queue
import org.jooq.lambda.fi.lang.CheckedRunnable

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
) : AbstractEngine<T, P>(environment) {

    /** Queue of updates to be processed after execution. */
    protected val afterExecutionUpdates: Queue<Update> = ArrayDeque()

    /** Manages dependencies between reactions in the simulation. */
    protected val dependencyGraph: DependencyGraph<T> = JGraphTDependencyGraph(environment)

    /**
     * Constructs a simulation with a default scheduler.
     *
     * This constructor initializes the simulation with a default [ArrayIndexedPriorityQueue].
     * If you need a custom [DependencyGraph] or [Scheduler], use the other constructor.
     *
     * @param environment the simulation environment
     */
    constructor(environment: Environment<T, P>) : this(environment, ArrayIndexedPriorityQueue())

    override fun initialize() {
        environment.globalReactions.forEach { reactionAdded(it) }
        environment.forEach { node -> node.reactions.forEach { scheduleReaction(it) } }
    }

    override fun doStep() {
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

    override fun processCommand(command: CheckedRunnable) {
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
     * Schedules a reaction by setting up dependencies and adding it to the scheduler.
     *
     * @param reaction the reaction to schedule
     */
    private fun scheduleReaction(reaction: Actionable<T>) {
        dependencyGraph.createDependencies(reaction)
        reaction.initializationComplete(time, environment)
        scheduler.addReaction(reaction)
    }

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
            get() = getReactionsRelatedTo(sourceNode, environment.getNeighborhood(sourceNode).current).filter {
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
                val sourceNeighbors = environment.getNeighborhood(sourceNode).current.asSequence()
                val targetNeighbors = environment.getNeighborhood(targetNode).current.asSequence()
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
}
