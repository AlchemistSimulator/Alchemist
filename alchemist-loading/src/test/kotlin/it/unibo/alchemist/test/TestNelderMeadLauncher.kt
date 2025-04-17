/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestNelderMeadLauncher() {
    val loader = LoadAlchemist
        .from(ResourceLoader.getResource("testNelderMeadLauncher.yml"))

    @Test
    fun `Nelder mead launcher should optimize the objective function as expected`() {
        val launcher = loader.launcher
        println("launcher is $launcher")
        launcher.launch(loader)
        val simulation = loader.getDefault<Any, Nothing>()
        assertEquals(simulation.environment.nodeCount , 1)
        simulation.play()
        simulation.run()
        val status = simulation.waitFor(Status.TERMINATED, 5, TimeUnit.MINUTES)
        check(status == Status.TERMINATED) {
            error("Simulation did not terminate after 5 minutes, status is $status")
        }
        // take the last created file at a specific path
        //  newestFileTime = max([os.path.getmtime(directory + '/' + file) for file in os.listdir(directory)], default=0.0)
        val newestFile = File("src/test/resources/testNelderMeadResults")
            .listFiles()
            ?.maxByOrNull { it.lastModified() }
       // from this file, i want to take all the values and relative keys of the object "variables"
        val json = newestFile?.readText()
        if(json != null) {
            val element = Json.parseToJsonElement(json)
            val variables = element.jsonObject["variables"]?.jsonObject
            println("variables are $variables")
            if (variables != null) {
                val inputs = variables.toMap().mapValues { it.value.toString().toDouble() }
                println("inputs are $inputs")
                loader.launch(DefaultLauncher())
                val optimizedSimulation = loader.getWith<Any, Nothing>(inputs)
                assertTrue(optimizedSimulation.environment.nodeCount in 75..85)
            }
        }
    }

    @Test
    fun `Nelder mead parameter optimization should TODO`() {
//        val newestFile = File("src/test/resources/testNelderMeadResults")
//            .listFiles()
//            ?.maxByOrNull { it.lastModified() }
//        // from this file, i want to take all the values and relative keys of the object "variables"
//        val json = newestFile?.readText()
//        if(json != null) {
//            val element = Json.parseToJsonElement(json)
//            val variables = element.jsonObject["variables"]?.jsonObject
//            println("variables are $variables")
//            if (variables != null) {
//                val inputs = variables.toMap().mapValues { it.value.toString().toDouble() }
//                println("inputs are $inputs")
//                loader.launch(DefaultLauncher())
//                val customVars = mapOf("width" to 9.0, "height" to 9.0)
//                val optimizedSimulation = loader.getWith<Any, Nothing>(customVars)
//                assertTrue(optimizedSimulation.environment.nodeCount in 75..85)
//            }
//        }
        loader.launch(DefaultLauncher())
        val customVars = mapOf("width" to 8.0, "height" to 10.0)
        val optimizedSimulation = loader.getWith<Any, Nothing>(customVars)
        optimizedSimulation.play()
        optimizedSimulation.run()
        assertTrue(optimizedSimulation.environment.nodeCount in 75..85)
    }
}

/**
 * The goal for the optimization.
 */
class Goal : (Environment<*, *>) -> Double {
    /**
     * The target number of nodes.
     */
    val target = 81

    override fun invoke(env: Environment<*, *>): Double = target.toDouble() - env.nodeCount
}

