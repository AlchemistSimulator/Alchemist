/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.environments.ImageEnvironment
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.kaikikm.threadresloader.ResourceLoader

/**
 * In this JUnit test Class you can control the parsing of some .png image to an
 * environment.
 *
 */
class TestImageEnvironment {

    private val images = listOf(
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
        "duelocalioreno-pianta3.png",
        "duelocalioreno-pianta3.png",
        "duelocalioreno-pianta3.png"
    )

    /**
     * Test the parsing of black and white images.
     */
    @Test
    fun testLoadingImages() {
        images.asSequence()
            .map { ResourceLoader.getResource(it).path }
            .flatMap { sequenceOf(ImageEnvironment<Any>(it), ImageEnvironment(it, MAX, MAX, MAX)) }
            .map { it.obstacles }
            .forEach { Assertions.assertTrue(it.isNotEmpty()) }
    }

    companion object {
        private const val MAX = 255.0
    }
}
