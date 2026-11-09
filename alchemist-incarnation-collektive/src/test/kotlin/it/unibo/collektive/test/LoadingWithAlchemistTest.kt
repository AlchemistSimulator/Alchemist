/*
 * Copyright (c) 2025, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

package it.unibo.collektive.test

import it.unibo.alchemist.CollektiveIncarnation
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.collektive.device.CollektiveDevice
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.ClassPathScanner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.apache.commons.math3.random.MersenneTwister

class LoadingWithAlchemistTest {
    @Test
    fun `test loading and running from resource folder`() {
        ClassPathScanner.resourcesMatching(".*\\.ya?ml", "it.unibo.collektive").forEach { simulationFile ->
            checkNotNull(Regex(".*/([^/]+?)$").matchEntire(simulationFile.path)).destructured
            val loader = LoadAlchemist.from(simulationFile)
            assertNotNull(loader)
            val simulation = loader.getDefault<Any, Nothing>()
            assertNotNull(simulation)
            simulation.environment.addTerminator(AfterTime(DoubleTime(10.0)))
            simulation.play()
            simulation.run()
        }
    }

    @Test
    fun `properties must be computable on non-collektive nodes`() {
        val incarnation = CollektiveIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val reference = SimpleMolecule("ref")
        val node = GenericNode(environment).apply { this[reference] = listOf(1, 2, 3) }
        assertEquals(
            3.0,
            incarnation.readConcentration(node, reference, "it[2]"),
        )
    }

    @Test
    fun `properties have access to the collektive device`() {
        val incarnation = CollektiveIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val randomGenerator = MersenneTwister(0)
        val reference = SimpleMolecule("ref")
        val node = GenericNode(environment).apply { addProperty(CollektiveDevice(randomGenerator, environment, this)) }
        assertEquals(
            0.0,
            incarnation.readConcentration(node, reference, "localId"),
        )
    }
}
