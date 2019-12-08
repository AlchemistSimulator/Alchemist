/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.loader.displacements.SpecificPositions
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import java.util.stream.Collectors

class TestSpecificPositions : StringSpec({
    "Test 2D specific positions" {
        val coordinates = arrayOf(listOf(1.0, 3.0), listOf(15.0, 10.0), listOf(0.0, 20.0))
        val env = Continuous2DEnvironment<Any>()
        val positions = SpecificPositions(env, *coordinates)
            .stream()
            .collect(Collectors.toList())
        positions shouldBe coordinates.map { env.makePosition(*it.toTypedArray()) }
    }
    "Wrong number of coordinates should fail" {
        shouldThrow<IllegalArgumentException> {
            SpecificPositions(Continuous2DEnvironment<Any>(), listOf(0.0, 1.0, 2.0))
        }
    }
})