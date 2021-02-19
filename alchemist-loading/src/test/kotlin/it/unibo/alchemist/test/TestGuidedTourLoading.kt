/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.konf.KonfBasedLoader
import it.unibo.alchemist.loader.konf.SupportedSpecType

class TestGuidedTourLoading : FreeSpec (
    {
        ClassPathScanner.resourcesMatching(".*\\.yml", "guidedTour").forEach { yaml ->
            "${yaml.file} should load with default parameters" {
                KonfBasedLoader(yaml.readText(), SupportedSpecType.YAML)
                    .getDefault<Any, Nothing>() shouldNotBe null
            }
        }
    }
)