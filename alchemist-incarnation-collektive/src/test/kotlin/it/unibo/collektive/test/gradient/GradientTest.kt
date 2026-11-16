/*
 * Copyright (c) 2023-2026, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

package it.unibo.collektive.test.gradient

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.fail

class GradientTest {
    @Test
    fun `a gradient is computed as the minimum distances from a source node to the other nodes`() {
        this::class.java.classLoader.getResource("it/unibo/collektive/test/gradient/gradient.yml")?.file?.let {
            val loader = LoadAlchemist.from(it)
            assertNotNull(loader)
            val simulation = loader.getDefault<Any, Nothing>()
            assertNotNull(simulation)
            simulation.environment.addTerminator(AfterTime(DoubleTime(10.0)))
            simulation.play()
            simulation.run()
            simulation.error.onSome { exception -> throw exception }
            assertEquals(10, simulation.environment.nodes.size.current)
            val moleculeToRead = SimpleMolecule("Gradient")
            val sourceNode = simulation.environment.nodes.current.first { node ->
                node[SimpleMolecule("source")].getOrNull() == true
            }
            simulation.environment.nodes.current.forEach { node ->
                val distance = simulation.environment.distanceBetween(sourceNode, node)
                assertEquals(distance, node[moleculeToRead].getOrNull() as Double, 1e-7)
            }
        } ?: fail("Failed to load alchemist file")
    }
}
