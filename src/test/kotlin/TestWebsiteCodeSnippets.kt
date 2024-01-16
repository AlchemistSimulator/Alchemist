import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.util.ClassPathScanner

/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestWebsiteCodeSnippets : FreeSpec(
    {
        val allSpecs = ClassPathScanner.resourcesMatching(".*", "website-snippets")
            .also { it shouldNot beEmpty() }
            .onEach { it shouldNot beNull() }
        allSpecs.forEach { url ->
            "snippet ${url.path.split("/").last()} should load correctly" - {
                val simulation = LoadAlchemist.from(url).getDefault<Any, Nothing>()
                simulation.shouldNotBeNull()
                val environment = simulation.environment
                environment.shouldNotBeNull()
                if (url.readText().contains("deployments:")) {
                    "and have deployed nodes" {
                        environment.shouldNotBeEmpty()
                        environment.nodes shouldNot beEmpty()
                    }
                } else {
                    "and be empty" {
                        environment.shouldBeEmpty()
                    }
                }
                "and execute a few steps without errors" {
                    environment.addTerminator(StepCount(100))
                    simulation.run()
                    simulation.error.shouldBeEmpty()
                }
            }
        }
    },
)
