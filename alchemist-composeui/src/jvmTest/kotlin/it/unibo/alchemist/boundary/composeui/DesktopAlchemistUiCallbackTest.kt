/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.SimulationControlsState
import it.unibo.alchemist.boundary.composeui.model.SimulationStatus
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import java.util.Optional
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jooq.lambda.fi.lang.CheckedRunnable

class DesktopAlchemistUiCallbackTest {
    @Test
    fun `to step resumes if the simulation was running`() {
        val simulation = RecordingSimulation(
            Status.RUNNING,
            DoubleTime(1.0),
            1L,
        )
        val store = ComposeUiStateStore(
            AlchemistUiState(
                controls = SimulationControlsState(status = SimulationStatus.RUNNING),
            ),
        )
        val callback = DesktopAlchemistUiCallback(simulation, store)

        store.update { it.copy(controls = it.controls.copy(toStepInput = "5")) }
        runSuspend {
            callback.onToStepSubmit()
        }

        assertEquals(2, simulation.playCalls)
        assertEquals(1, simulation.goToStepCalls)
        assertEquals(Status.RUNNING, simulation.statusValue)
        assertEquals(5L, simulation.stepValue)
        assertTrue(store.state.controls.status == SimulationStatus.RUNNING)
        assertEquals(5L, store.state.controls.step)
    }

    @Test
    fun `to time resumes if the simulation was running`() {
        val simulation = RecordingSimulation(
            Status.RUNNING,
            DoubleTime(1.0),
            1L,
        )
        val store = ComposeUiStateStore(
            AlchemistUiState(
                controls = SimulationControlsState(status = SimulationStatus.RUNNING),
            ),
        )
        val callback = DesktopAlchemistUiCallback(simulation, store)

        store.update { it.copy(controls = it.controls.copy(toTimeInput = "4.5")) }
        runSuspend {
            callback.onToTimeSubmit()
        }

        assertEquals(2, simulation.playCalls)
        assertEquals(1, simulation.goToTimeCalls)
        assertEquals(Status.RUNNING, simulation.statusValue)
        assertEquals(DoubleTime(4.5), simulation.timeValue)
        assertTrue(store.state.controls.status == SimulationStatus.RUNNING)
        assertEquals("4.50", store.state.controls.timeLabel)
    }
}

private class RecordingSimulation(
    initialStatus: Status,
    initialTime: Time,
    initialStep: Long,
) : Simulation<Any, StubPosition> {
    var statusValue: Status = initialStatus
    var timeValue: Time = initialTime
    var stepValue: Long = initialStep
    var playCalls: Int = 0
    var pauseCalls: Int = 0
    var goToTimeCalls: Int = 0
    var goToStepCalls: Int = 0

    override fun addOutputMonitor(op: it.unibo.alchemist.boundary.OutputMonitor<Any, StubPosition>) = Unit

    override fun getEnvironment(): Environment<Any, StubPosition> = error("not used")

    override fun getError(): Optional<Throwable> = Optional.empty()

    override fun getStatus(): Status = statusValue

    override fun getStep(): Long = stepValue

    override fun getTime(): Time = timeValue

    override fun goToStep(step: Long): CompletableFuture<Unit> {
        goToStepCalls++
        stepValue = step
        statusValue = Status.PAUSED
        return completed()
    }

    override fun goToTime(t: Time): CompletableFuture<Unit> {
        goToTimeCalls++
        timeValue = t
        statusValue = Status.PAUSED
        return completed()
    }

    override fun neighborAdded(node: Node<Any>, n: Node<Any>) = Unit

    override fun neighborRemoved(node: Node<Any>, n: Node<Any>) = Unit

    override fun nodeAdded(node: Node<Any>) = Unit

    override fun nodeMoved(node: Node<Any>) = Unit

    override fun nodeRemoved(node: Node<Any>, oldNeighborhood: Neighborhood<Any>) = Unit

    override fun pause(): CompletableFuture<Unit> {
        pauseCalls++
        statusValue = Status.PAUSED
        return completed()
    }

    override fun play(): CompletableFuture<Unit> {
        playCalls++
        statusValue = Status.RUNNING
        return completed()
    }

    override fun reactionAdded(reactionToAdd: Actionable<Any>) = Unit

    override fun reactionRemoved(reactionToRemove: Actionable<Any>) = Unit

    override fun removeOutputMonitor(op: it.unibo.alchemist.boundary.OutputMonitor<Any, StubPosition>) = Unit

    override fun schedule(r: CheckedRunnable) = error("not used")

    override fun terminate(): CompletableFuture<Unit> {
        statusValue = Status.TERMINATED
        return completed()
    }

    override fun waitFor(s: Status, timeout: Long, timeunit: java.util.concurrent.TimeUnit): Status {
        statusValue = s
        return statusValue
    }

    override fun getOutputMonitors(): List<it.unibo.alchemist.boundary.OutputMonitor<Any, StubPosition>> = emptyList()

    override fun run() = Unit

    private fun completed(): CompletableFuture<Unit> = CompletableFuture.completedFuture(Unit)
}

private data class StubPosition(private val coordinate: Double = 0.0) : Position<StubPosition> {
    override fun boundingBox(range: Double): List<StubPosition> = listOf(this)

    override val coordinates: DoubleArray = doubleArrayOf(coordinate)

    override fun getCoordinate(dimension: Int): Double = coordinate

    override val dimensions: Int = 1

    override fun distanceTo(other: StubPosition): Double = kotlin.math.abs(coordinate - other.coordinate)

    override operator fun plus(other: DoubleArray): StubPosition = copy(coordinate = coordinate + (other.firstOrNull() ?: 0.0))

    override operator fun minus(other: DoubleArray): StubPosition = copy(coordinate = coordinate - (other.firstOrNull() ?: 0.0))
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
