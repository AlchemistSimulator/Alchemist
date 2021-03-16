/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.SupportedIncarnations
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.displacements.SpecificPositions
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import org.kaikikm.threadresloader.ResourceLoader
import java.util.stream.Collectors

class TestSpecificPositions : StringSpec({
    "Test 2D specific positions" {
        val coordinates = arrayOf(listOf(1.0, 3.0), listOf(15.0, 10.0), listOf(0.0, 20.0))
        val env = Continuous2DEnvironment<Any>(incarnation())
        val positions = SpecificPositions(env, *coordinates)
            .stream()
            .collect(Collectors.toList())
        positions shouldBe coordinates.map { env.makePosition(*it.toTypedArray()) }
    }
    "Wrong number of coordinates should fail" {
        shouldThrow<IllegalArgumentException> {
            SpecificPositions(Continuous2DEnvironment<Any>(incarnation()), listOf(0.0, 1.0, 2.0))
        }
    }
    "Test YAML loading with 2D env" {
        val loader = LoadAlchemist.from(ResourceLoader.getResource("testSpecificPositions.yml"))
        val env = loader.getWith<Any, Euclidean2DPosition>(emptyMap<String, Double>()).environment
        env.nodes.map { env.getPosition(it) } shouldBe listOf(Euclidean2DPosition(1.0, 2.0), Euclidean2DPosition(3.0, 4.0))
    }
}) {
    companion object {
        private fun incarnation() = SupportedIncarnations.get<Any, Euclidean2DPosition>("sapere").get()
    }
}
