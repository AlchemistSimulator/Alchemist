/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.adapter.toViewport
import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.NodePositionUpdate
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.times.DoubleTime
import java.util.Optional
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import org.apache.commons.math3.random.RandomGenerator
import org.jooq.lambda.fi.lang.CheckedRunnable

class DesktopAlchemistUiNodeMoveTest {
    @Test
    fun `node move commits translated positions through the simulator and refreshes the scene`() {
        val environment = Continuous2DEnvironment(TestIncarnation())
        val firstNode = GenericNode<Any>(environment)
        val secondNode = GenericNode<Any>(environment)
        environment.addNode(firstNode, environment.makePosition(0.0, 0.0))
        environment.addNode(secondNode, environment.makePosition(2.0, 3.0))
        val simulation = RecordingMoveSimulation(environment)
        val store = ComposeUiStateStore(
            AlchemistUiState(
                scene = environment.toViewport(),
                selectedNodeIds = listOf(firstNode.id, secondNode.id),
            ).withSelection(listOf(firstNode.id, secondNode.id)),
        )
        val callback = DesktopAlchemistUiCallback(simulation, store)

        runSuspend {
            callback.onNodesMoved(
                listOf(
                    NodePositionUpdate(firstNode.id, listOf(1.5, -2.0)),
                    NodePositionUpdate(secondNode.id, listOf(3.5, 1.0)),
                ),
            )
        }

        assertEquals(1, simulation.scheduleCalls)
        assertEquals(listOf(firstNode.id, secondNode.id), simulation.movedNodeIds)
        assertEquals(listOf(1.5, -2.0), environment.getPosition(firstNode).coordinates.toList())
        assertEquals(listOf(3.5, 1.0), environment.getPosition(secondNode).coordinates.toList())
        assertEquals(listOf(firstNode.id, secondNode.id), store.state.selectedNodeIds)
        assertEquals(
            listOf(1.5, -2.0),
            store.state.scene.nodes.first { it.id == firstNode.id }.coordinates,
        )
        assertEquals(
            listOf(3.5, 1.0),
            store.state.scene.nodes.first { it.id == secondNode.id }.coordinates,
        )
    }
}

private class RecordingMoveSimulation(
    private val environmentValue: Environment<Any, Euclidean2DPosition>,
) : Simulation<Any, Euclidean2DPosition> {
    var statusValue: Status = Status.PAUSED
    var timeValue: Time = DoubleTime(0.0)
    var stepValue: Long = 0L
    var scheduleCalls: Int = 0
    val movedNodeIds = mutableListOf<Int>()

    override fun addOutputMonitor(op: it.unibo.alchemist.boundary.OutputMonitor<Any, Euclidean2DPosition>) = Unit

    override fun getEnvironment(): Environment<Any, Euclidean2DPosition> = environmentValue

    override fun getError(): Optional<Throwable> = Optional.empty()

    override fun getStatus(): Status = statusValue

    override fun getStep(): Long = stepValue

    override fun getTime(): Time = timeValue

    override fun goToStep(step: Long): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    override fun goToTime(t: Time): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    override fun neighborAdded(node: Node<Any>, n: Node<Any>) = Unit

    override fun neighborRemoved(node: Node<Any>, n: Node<Any>) = Unit

    override fun nodeAdded(node: Node<Any>) = Unit

    override fun nodeMoved(node: Node<Any>) {
        movedNodeIds += node.id
    }

    override fun nodeRemoved(node: Node<Any>, oldNeighborhood: Neighborhood<Any>) = Unit

    override fun pause(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    override fun play(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    override fun reactionAdded(reactionToAdd: Actionable<Any>) = Unit

    override fun reactionRemoved(reactionToRemove: Actionable<Any>) = Unit

    override fun removeOutputMonitor(op: it.unibo.alchemist.boundary.OutputMonitor<Any, Euclidean2DPosition>) = Unit

    override fun schedule(r: CheckedRunnable) {
        scheduleCalls++
        r.run()
    }

    override fun terminate(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)

    override fun waitFor(s: Status, timeout: Long, timeunit: TimeUnit): Status = s

    override fun getOutputMonitors(): List<it.unibo.alchemist.boundary.OutputMonitor<Any, Euclidean2DPosition>> =
        emptyList()

    override fun run() = Unit
}

private class TestIncarnation : Incarnation<Any, Euclidean2DPosition> {
    override fun getProperty(node: Node<Any>, molecule: Molecule, property: String): Double = Double.NaN

    override fun createMolecule(s: String): Molecule = SimpleMolecule(s)

    override fun createConcentration(descriptor: Any?): Any = descriptor ?: Unit

    override fun createConcentration(): Any = Unit

    override fun createNode(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, Euclidean2DPosition>,
        parameter: Any?,
    ): Node<Any> = GenericNode(environment)

    override fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, Euclidean2DPosition>,
        node: Node<Any>?,
        parameter: Any?,
    ): TimeDistribution<Any> = error("not used")

    override fun createReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, Euclidean2DPosition>,
        node: Node<Any>,
        timeDistribution: TimeDistribution<Any>,
        parameter: Any?,
    ): Reaction<Any> = error("not used")

    override fun createCondition(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, Euclidean2DPosition>,
        node: Node<Any>?,
        actionable: Actionable<Any>,
        additionalParameters: Any?,
    ): Condition<Any> = error("not used")

    override fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<Any, Euclidean2DPosition>,
        node: Node<Any>?,
        actionable: Actionable<Any>,
        additionalParameters: Any?,
    ): Action<Any> = error("not used")
}

private fun runSuspend(block: suspend () -> Unit) {
    block.startCoroutine(
        object : Continuation<Unit> {
            override val context = EmptyCoroutineContext

            override fun resumeWith(result: Result<Unit>) {
                result.getOrThrow()
            }
        },
    )
}
