/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.util.ClassPathScanner
import java.io.File
import java.lang.RuntimeException
import java.net.URL

private val cache: LoadingCache<URL, Loader> = Caffeine.newBuilder().build {
    LoadAlchemist.from(it)
}

class TestGuidedTourLoading : FreeSpec(
    {
        ClassPathScanner.resourcesMatching(".*\\.[yY][aA]?[mM][lL]", "guidedTour").forEach { yaml ->
            "${File(yaml.file).name} should load with default parameters" {
                cache.get(yaml)?.getDefault<Any, Nothing>() shouldNotBe null
            }
        }
        ClassPathScanner.resourcesMatching(".*[Vv]ariable.*\\.yml", "guidedTour").forEach { yaml ->
            "${File(yaml.file).name} should actually define variables" {
                val parsed = cache.get(yaml)!!
                (parsed.variables + parsed.dependentVariables).size shouldBeGreaterThan 0
            }
        }
        ClassPathScanner.resourcesMatching(".*\\.[yY][aA]?[mM][lL]", "failures").forEach { yaml ->
            "${File(yaml.file).name} should not load" {
                shouldThrow<RuntimeException> { LoadAlchemist.from(yaml).getDefault<Any, Nothing>() }
            }
        }
    },
)
