/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.TimeUnit
import kotlin.test.assertContentEquals
import org.junit.jupiter.api.Assertions.assertEquals

private fun <T> Iterable<T>.ebeEquals(other: Iterable<T>, elementComparator: (T, T) -> Unit) {
    val thisList = this.toList()
    val otherList = other.toList()
    assertEquals(otherList.size, thisList.size)
    otherList.zip(thisList).forEach { (expected, actual) ->
        elementComparator(expected, actual)
    }
}

infix fun Condition<*>.shouldEqual(other: Condition<*>) {
    assertEquals(other::class, this::class, "Condition types don't match")
    assertEquals(other.context, context, "Condition contexts don't match")
    assertEquals(other.isValid, isValid, "Condition validity doesn't match")
    assertEquals(other.propensityContribution, propensityContribution)
    assertEquals(other.inboundDependencies, inboundDependencies)
}

infix fun Action<*>.shouldEqual(other: Action<*>) {
    assertEquals(other::class, this::class, "Action types don't match")
    assertEquals(other.context, context, "Action contexts don't match")
    assertEquals(other.outboundDependencies, outboundDependencies)
}

infix fun TimeDistribution<*>.shouldEqual(other: TimeDistribution<*>) {
    assertEquals(other::class, this::class, "TimeDistribution types don't match")
    assertEquals(other.rate, rate)
    assertEquals(other.nextOccurence, nextOccurence)
}

infix fun Actionable<*>.shouldEqual(other: Actionable<*>) {
    assertEquals(other::class, this::class, "Actionable types don't match")
    assertEquals(other.inboundDependencies, inboundDependencies)
    assertEquals(other.outboundDependencies, outboundDependencies)
    assertEquals(other.rate, rate)
    assertEquals(other.tau, tau)
    timeDistribution shouldEqual other.timeDistribution
    conditions.ebeEquals(other.conditions) { expected, actual -> actual shouldEqual expected }
    actions.ebeEquals(other.actions) { expected, actual -> actual shouldEqual expected }
}

infix fun NodeProperty<*>.shouldEqual(other: NodeProperty<*>) {
    assertEquals(other::class, this::class, "NodeProperty types don't match")
}

infix fun Node<*>.shouldEqual(other: Node<*>) {
    assertEquals(other.id, id)
    assertEquals(other.moleculeCount, moleculeCount)
    assertEquals(other.contents, contents)
    assertEquals(other.properties.size, properties.size)
    properties.ebeEquals(other.properties) { expected, actual -> actual shouldEqual expected }
    reactions.ebeEquals(other.reactions) { expected, actual -> actual shouldEqual expected }
}

infix fun LinkingRule<*, *>.shouldEqual(other: LinkingRule<*, *>) {
    assertEquals(other::class, this::class, "LinkingRule types don't match")
    assertEquals(other.isLocallyConsistent, isLocallyConsistent)
}

infix fun <T, P : Position<P>> Environment<T, P>.shouldEqual(other: Environment<T, P>) {
    assertEquals(other::class, this::class, "Environment types don't match")
    assertContentEquals(other.size, size, "Environment sizes don't match")
    assertContentEquals(other.sizeInDistanceUnits, sizeInDistanceUnits)
    assertEquals(other.incarnation::class, incarnation::class)
    assertEquals(other.dimensions, dimensions)
    assertEquals(other.isTerminated, isTerminated)
    assertContentEquals(other.sizeInDistanceUnits, sizeInDistanceUnits)
    linkingRule shouldEqual other.linkingRule
    val positions = nodes.sortedBy { it.id }.map { getPosition(it) }
    val otherPositions = other.sortedBy { it.id }.map { getPosition(it) }
    positions.ebeEquals(otherPositions) { expected, actual -> assertEquals(expected, actual) }
    nodes.ebeEquals(other.nodes) { expected, actual ->
        actual shouldEqual expected
    }
    globalReactions.ebeEquals(other.globalReactions) { expected, actual -> actual shouldEqual expected }
    layers.toList().sortedBy { (molecule, _) -> molecule.toString() }.ebeEquals(
        other.layers.toList().sortedBy { (molecule, _) -> molecule.toString() },
    ) { expected, actual ->
        assertEquals(expected.first, actual.first, "Layer molecules don't match")
        positions.forEach { position ->
            assertEquals(
                expected.second.getValue(position),
                actual.second.getValue(position),
                "Layer values at position $position don't match",
            )
        }
    }
}

fun <T, P : Position<P>> Simulation<T, P>.equalsForSteps(other: Simulation<T, P>, steps: Long) {
    assertEquals(other::class, this::class, "Simulation types don't match")
    outputMonitors.sortedBy {
        it::class.simpleName
    }.ebeEquals(other.outputMonitors.sortedBy { it::class.simpleName }) { expected, actual ->
        assertEquals(expected::class, actual::class, "OutputMonitor types don't match")
    }
    val expectedThread = Thread(null, other, "Alchemist-Test-Expected")
    val actualThread = Thread.currentThread()
    val syncMonitor = object : OutputMonitor<T, P> {
        val barrier = CyclicBarrier(2)
        override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
            barrier.await(1, TimeUnit.SECONDS)
            if (barrier.isBroken) {
                error("Barrier broken while waiting for step $step to complete")
            }
            if (Thread.currentThread() == actualThread) {
                assertEquals(other.time, this@equalsForSteps.time)
                assertEquals(other.step, this@equalsForSteps.step)
                this@equalsForSteps.environment shouldEqual other.environment
            }
            if (environment.simulation.step >= steps) {
                environment.simulation.terminate()
            }
            barrier.await(1, TimeUnit.SECONDS)
        }
    }
    addOutputMonitor(syncMonitor)
    other.addOutputMonitor(syncMonitor)
    expectedThread.start()
    other.play()
    runInCurrentThread()
    error.map { throw it }
    other.error.map { throw it }
}
