/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.kotlindsl.TestComparators.shouldEqual
import java.nio.file.Files
import java.util.stream.Stream
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.kaikikm.threadresloader.ResourceLoader

class KotlinDslProviderTest {

    @Test
    fun `a simple textual Kotlin DSL script should load`() {
        val script = """
            simulation2D(SAPEREIncarnation()) {
                environment {
                    networkModel(ConnectWithinDistance(5.0))
                    deployments {
                        deploy(point(0.0, 0.0))
                        deploy(point(0.0, 1.0))
                    }
                }
            }
        """.trimIndent()
        val path = Files.createTempFile("dsl-test-", ".alchemist.kts")
        path.writeText(script)
        val loader = LoadAlchemist.from(path.toFile())
        assertNotNull(loader)
    }

    @ParameterizedTest(name = "{0} should match {1}")
    @MethodSource("equivalenceCases")
    fun `Kotlin DSL resources should match YAML equivalents`(ktsResource: String, ymlResource: String) {
        val url = requireNotNull(ResourceLoader.getResource(ktsResource)) {
            "Resource $ktsResource not found on test classpath"
        }
        LoadAlchemist.from(url).shouldEqual<Any, Nothing>(ymlResource, steps = 100L)
    }

    companion object {
        @JvmStatic
        fun equivalenceCases(): Stream<Arguments> = Stream.of(
            Arguments.of("dsl/kts/12-layers.alchemist.kts", "dsl/yml/12-layers.yml"),
            Arguments.of("dsl/kts/14-exporters.alchemist.kts", "dsl/yml/14-exporters.yml"),
            Arguments.of("dsl/kts/15-variables.alchemist.kts", "dsl/yml/15-variables.yml"),
            Arguments.of("dsl/kts/18-properties.alchemist.kts", "dsl/yml/18-properties.yml"),
            Arguments.of("dsl/kts/19-performance.alchemist.kts", "dsl/yml/19-performance.yml"),
        )
    }
}
