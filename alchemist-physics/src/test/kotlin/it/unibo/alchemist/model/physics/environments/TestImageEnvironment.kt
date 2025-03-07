/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.physics.environments

import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kaikikm.threadresloader.ResourceLoader

/**
 * In this JUnit test Class you can control the parsing of some .png image to an
 * environment.
 *
 */
class TestImageEnvironment {
    private val images =
        listOf(
            "piantina1.png",
            "planimetriabn1.png",
            "piantina2.png",
            "piantina3.png",
            "piantina4.png",
            "piantina5.png",
            "piantina6.png",
            "piantina7.png",
            "piantina8.png",
            "piantina9.png",
            "Pastorello.png",
            "Senzanome.png",
            "duelocalioreno-pianta3.png",
            "2rettangolo_nero.png",
            "PlanimetriaChiaravalle1.png",
        )

    /**
     * Test the parsing of black and white images.
     */
    @Test
    fun testLoadingImages() {
        val incarnation = SupportedIncarnations.get<Any, Euclidean2DPosition>("protelis").orElseGet { TODO() }
        images
            .asSequence()
            .map { it to ResourceLoader.getResource("images/$it") }
            .map { it.second ?: error("Could not lead image ${it.first}") }
            .map { it.path }
            .flatMap {
                sequenceOf(
                    ImageEnvironment<Any>(incarnation, it),
                    ImageEnvironment(incarnation, it, MAX, MAX, MAX),
                )
            }.map { it.obstacles }
            .forEach { Assertions.assertTrue(it.isNotEmpty()) }
    }

    companion object {
        private const val MAX = 255.0
    }
}
