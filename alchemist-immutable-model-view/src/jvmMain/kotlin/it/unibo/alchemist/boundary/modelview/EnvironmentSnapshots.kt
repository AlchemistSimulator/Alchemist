/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import it.unibo.alchemist.core.SimulationState
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistributedReaction
import java.util.PriorityQueue

/** Builds snapshots of a running simulation. */
object EnvironmentSnapshots {
    /**
     * This environment at [time] and [step], holding what [request] asks for.
     * Call it on the simulation thread, between two steps, so that every part refers to the same moment.
     */
    fun <T, P : Position<P>> Environment<T, P>.snapshot(
        request: SnapshotRequest = SnapshotRequest(),
        time: Double = simulation.currentTime.toDouble(),
        step: Long = simulation.step,
    ): SimulationSnapshot {
        val molecules = request.values.associateWith(incarnation::createMolecule)
        return SimulationSnapshot(
            time = time,
            step = step,
            status = simulation.state.toStatus(),
            dimensions = dimensions,
            nodes = nodes.current.map { node ->
                NodeSnapshot(
                    id = node.id,
                    coordinates = currentPositionOf(node).coordinates.toList(),
                    values = molecules
                        .mapNotNull { (name, molecule) ->
                            node.contents.current[molecule]?.let { name to ValueSnapshot.of(it, request.valueLimits) }
                        }
                        .toMap(),
                    details = node.details(request.valueLimits).takeIf { node.id in request.detailed },
                )
            },
            links = request.maxLinks?.let { links(it) },
        )
    }

    /** This reaction as sent. */
    fun Reaction<*>.toSnapshot(): ReactionSnapshot = ReactionSnapshot(
        type = this::class.simpleName ?: "Reaction",
        nextOccurrence = nextOccurrence.current.toDouble(),
        executable = canExecute.current,
        meanDelay = (this as? TimeDistributedReaction<*>)?.meanDelay,
        timeDistribution = (this as? TimeDistributedReaction<*>)?.timeDistribution?.toString(),
        conditions = conditions.map(Any::toString),
        actions = actions.map(Any::toString),
        text = toString(),
    )

    private fun Node<*>.details(limits: ValueLimits) = NodeDetails(
        contents = contents.current.map { (molecule, concentration) ->
            ContentSnapshot(
                molecule = molecule.toString(),
                value = ValueSnapshot.of(concentration, limits),
                type = concentration?.let { it::class.simpleName } ?: "null",
            )
        },
        reactions = reactions.current.map { it.toSnapshot() },
        properties = properties.map(Any::toString),
    )

    /** Every link once, or the [max] with the lowest score, found without sorting them all. */
    private fun <T> Environment<T, *>.links(max: Int): LinksSnapshot {
        val keys = nodes.current
            .asSequence()
            .flatMap { node -> neighborhoodOf(node).current.neighbors.asSequence().map { node.id to it.id } }
            .mapNotNull { (first, second) -> LinkKey.of(first, second) }
            .toHashSet()
        val lowest = PriorityQueue(max + 1, compareByDescending(LinkKey::sampleScore))
        keys.forEach { key ->
            lowest.add(key)
            lowest.takeIf { it.size > max }?.poll()
        }
        return LinksSnapshot(lowest.sortedBy(LinkKey::sampleScore).map(LinkKey::snapshot), total = keys.size)
    }

    private fun SimulationState.toStatus(): SimulationStatus = when (this) {
        SimulationState.INIT -> SimulationStatus.INIT
        SimulationState.READY -> SimulationStatus.READY
        SimulationState.PAUSED -> SimulationStatus.PAUSED
        SimulationState.RUNNING -> SimulationStatus.RUNNING
        SimulationState.TERMINATED -> SimulationStatus.TERMINATED
    }
}
