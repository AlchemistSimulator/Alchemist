import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.terminators.StepCount
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import it.unibo.alchemist.util.ClassPathScanner
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

class TestWebsiteCodeSnippets {
    @Test
    fun `All website code snippets should load and execute correctly`() =
        runTest(timeout = 30.minutes) {
            val allSpecs = ClassPathScanner.resourcesMatching(".*", "website-snippets")
            assertFalse(allSpecs.isEmpty(), "No website snippets found")
            allSpecs
                .map { url ->
                    launch {
                        assertNotNull(url, "Resource URL should not be null")
                        val snippetName = url.path.split("/").last()
                        println("Testing snippet: $snippetName")
                        val simulation = LoadAlchemist.from(url).getDefault<Any, Nothing>()
                        assertNotNull(simulation, "Simulation should load correctly")
                        val environment = simulation.environment
                        assertNotNull(environment, "Environment should not be null")
                        if (url.readText().contains("deployments:")) {
                            assertFalse(environment.nodes.isEmpty(), "Expected deployed nodes but found none")
                        } else {
                            assertTrue(environment.nodes.isEmpty(), "Expected an empty environment but found nodes")
                        }
                        environment.addTerminator(StepCount(100))
                        val errorContainer = simulation.runInCurrentThread().error
                        assertTrue(errorContainer.isEmpty, "Simulation encountered errors: $errorContainer")
                    }
                }.forEach {
                    it.join()
                }
        }
}
