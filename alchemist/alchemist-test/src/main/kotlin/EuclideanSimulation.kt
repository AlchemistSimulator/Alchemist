/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.EuclideanEnvironment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.kaikikm.threadresloader.ResourceLoader
import java.lang.IllegalStateException

/**
 * Run the simulation this environment owns.
 *
 * @param initialized
 *          the lambda to execute when the simulation begins.
 * @param stepDone
 *          the lambda to execute on each step of the simulation.
 * @param finished
 *          the lambda to execute at the end of the simulation.
 * @param steps
 *          the number of steps the simulation must execute.
 */
fun <T, P> EuclideanEnvironment<T, P>.startSimulation(
    initialized: (EuclideanEnvironment<T, P>) -> Unit = { },
    stepDone: (EuclideanEnvironment<T, P>, Reaction<T>, Time, Long) -> Unit = { _, _, _, _ -> Unit },
    finished: (EuclideanEnvironment<T, P>, Time, Long) -> Unit = { _, _, _ -> Unit },
    steps: Long = 10000
): EuclideanEnvironment<T, P> where P : Position<P>, P : Vector<P> =
    Engine(this, steps).apply {
        addOutputMonitor(
            object : OutputMonitor<T, P> {
                override fun initialized(environment: Environment<T, P>) =
                    initialized.invoke(this@startSimulation)
                override fun stepDone(environment: Environment<T, P>, reaction: Reaction<T>, t: Time, s: Long) =
                    stepDone.invoke(this@startSimulation, reaction, t, s)
                override fun finished(environment: Environment<T, P>, t: Time, s: Long) =
                    finished.invoke(this@startSimulation, t, s)
            }
        )
        play()
        run()
        error.ifPresent { throw it }
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
    vars: Map<String, Double> = emptyMap()
): EuclideanEnvironment<T, P> where P : Position<P>, P : Vector<P> =
    YamlLoader(ResourceLoader.getResourceAsStream(resource)).getWith<T, P>(vars).let {
        if (it is EuclideanEnvironment) it else throw IllegalStateException("Illegal kind of environment")
    }
