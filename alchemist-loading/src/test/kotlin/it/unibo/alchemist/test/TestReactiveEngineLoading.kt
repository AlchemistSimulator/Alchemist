/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.core.ReactiveEngine
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.kaikikm.threadresloader.ResourceLoader

class TestReactiveEngineLoading : StringSpec({

    "It should be possible to load a simulation with the ReactiveEngine via YAML overrides" {
        val yaml = "synthetic/scalavar.yml"
        val overrides = listOf(
            """
            engine:
                type: it.unibo.alchemist.core.ReactiveEngine
            """.trimIndent(),
        )

        val resource = ResourceLoader.getResource(yaml)
        val loader = LoadAlchemist.from(resource, overrides)
        val simulation = loader.getDefault<Any, Euclidean2DPosition>()

        simulation.shouldBeInstanceOf<ReactiveEngine<Any, Euclidean2DPosition>>()

        simulation.play()
        simulation.run()
        simulation.error.isPresent shouldBe false
    }
})
