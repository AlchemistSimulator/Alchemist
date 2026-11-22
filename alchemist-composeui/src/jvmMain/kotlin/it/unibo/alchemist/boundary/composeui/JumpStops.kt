/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.updateAndGet

/**
 * Where a jumping [simulation] pauses: at the earliest stop it reaches, which drops all the others.
 * The end of the simulation drops them too.
 * Register it as a monitor of the [simulation].
 */
internal class JumpStops<T, P : Position<P>>(private val simulation: Simulation<T, P>) : OutputMonitor<T, P> {
    private val stops = MutableStateFlow(Stops())

    /**
     * Adds a stop at step [target].
     * The result completes once the simulation pauses at any stop, or the stops are dropped.
     */
    fun atStep(target: Long): Deferred<Unit> =
        stops.updateAndGet { it.copy(step = it.step?.coerceAtMost(target) ?: target) }.done

    /**
     * Adds a stop at time [target].
     * The result completes once the simulation pauses at any stop, or the stops are dropped.
     */
    fun atTime(target: Double): Deferred<Unit> =
        stops.updateAndGet { it.copy(time = it.time?.coerceAtMost(target) ?: target) }.done

    /** Drops every stop, for a command that overrides them. */
    fun clear() {
        stops.getAndUpdate { Stops() }.done.complete(Unit)
    }

    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        clear()
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        val current = stops.value
        // The step argument counts the steps before this one: the engine's counter includes it, as in goToStep.
        if (current.reachedAt(simulation.step, time.toDouble()) && stops.compareAndSet(current, Stops())) {
            simulation.pause().thenRun { current.done.complete(Unit) }
        }
    }

    /** The pending stops, and what completes once the simulation pauses at one of them. */
    private data class Stops(
        val step: Long? = null,
        val time: Double? = null,
        val done: CompletableDeferred<Unit> = CompletableDeferred(),
    ) {
        fun reachedAt(step: Long, time: Double): Boolean =
            this.step?.let { step >= it } == true || this.time?.let { time >= it } == true
    }
}
