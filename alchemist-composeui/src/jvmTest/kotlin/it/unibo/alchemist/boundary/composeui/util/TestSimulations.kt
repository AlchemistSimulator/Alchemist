/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.util

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.genericReaction
import it.unibo.alchemist.model.timedistributions.DiracComb
import kotlin.concurrent.thread

/**
 * Simulations and engine drivers shared by the JVM tests of the Compose UI.
 */
internal object TestSimulations {
    /**
     * Loads a 2D simulation with one node per entry of [positions].
     * Every node fires an empty reaction once per time unit, so the simulation never runs out of events.
     */
    fun ticking(vararg positions: Pair<Double, Double>): Simulation<Any, Euclidean2DPosition> =
        simulation2D(SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseThrow()) {
            environment {
                deployments {
                    positions.forEach { (x, y) ->
                        deploy(point(x, y)) {
                            withTimeDistribution(DiracComb<Any>(1.0)) {
                                program(genericReaction())
                            }
                        }
                    }
                }
            }
        }.getDefault<Any, Euclidean2DPosition>()

    /**
     * Runs [action] once, on the simulation thread, right after the first step completes.
     * While [action] runs, the simulation is running and cannot make further progress.
     */
    fun <T, P : Position<out P>> Simulation<T, P>.onFirstStep(action: (Simulation<T, P>) -> Unit) {
        val simulation = this
        addOutputMonitor(
            object : OutputMonitor<T, P> {
                override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
                    simulation.removeOutputMonitor(this)
                    action(simulation)
                }
            },
        )
    }

    /**
     * Runs this simulation on a dedicated thread while [block] executes,
     * then terminates it and waits for the thread to end.
     */
    fun <T, P : Position<out P>, R> Simulation<T, P>.runningInBackground(block: () -> R): R {
        val engine = thread(name = "Compose UI test simulation") { run() }
        return try {
            block()
        } finally {
            terminate()
            engine.join()
        }
    }
}
