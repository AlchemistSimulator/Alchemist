/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.testsupport

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.geometry.Vector
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Run the simulation this environment owns.
 *
 * @param onceInitialized
 *          the lambda to execute when the simulation begins.
 * @param atEachStep
 *          the lambda to execute on each step of the simulation.
 * @param whenFinished
 *          the lambda to execute at the end of the simulation.
 * @param steps
 *          the number of steps the simulation must execute.
 */
fun <T, P> EuclideanEnvironment<T, P>.startSimulation(
    onceInitialized: (EuclideanEnvironment<T, P>) -> Unit = { },
    atEachStep: (EuclideanEnvironment<T, P>, Actionable<T>?, Time, Long) -> Unit = { _, _, _, _ -> },
    whenFinished: (EuclideanEnvironment<T, P>, Time, Long) -> Unit = { _, _, _ -> },
    steps: Long = 10000,
) where P : Position<P>, P : Vector<P> =
    Engine(this, steps).apply {
        fun checkForErrors() = error.ifPresent { throw it }
        addOutputMonitor(
            object : OutputMonitor<T, P> {
                override fun initialized(environment: Environment<T, P>) {
                    checkForErrors()
                    onceInitialized(this@startSimulation)
                }
                override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, t: Time, s: Long) {
                    checkForErrors()
                    atEachStep(this@startSimulation, reaction, t, s)
                }
                override fun finished(environment: Environment<T, P>, t: Time, s: Long) {
                    checkForErrors()
                    whenFinished(this@startSimulation, t, s)
                }
            },
        )
        play()
        run()
        checkForErrors()
    }.environment as EuclideanEnvironment<T, P>

/**
 * Loads a simulation from a YAML file.
 *
 * @param resource
 *          the name of the file containing the simulation to load.
 * @param vars
 *          a map specifying name-value bindings for the variables in this scenario.
 */
fun <T, P> loadYamlSimulation(
    resource: String,
    vars: Map<String, Double> = emptyMap(),
): EuclideanEnvironment<T, P> where P : Position<P>, P : Vector<P> =
    LoadAlchemist.from(ResourceLoader.getResource(resource)).getWith<T, P>(vars).environment
        .let { it as? EuclideanEnvironment } ?: error("Illegal kind of environment")
