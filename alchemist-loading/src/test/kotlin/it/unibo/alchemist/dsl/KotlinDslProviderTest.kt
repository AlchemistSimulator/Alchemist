/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.LoadAlchemist
import java.io.File
import java.nio.file.Files
import kotlin.io.path.writeText
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class KotlinDslProviderTest {
    @Test
    fun loadSimpleScript() {
        val script = """
            val inc = SAPERE.incarnation<Any, Euclidean2DPosition>()
            simulation(inc) {
                networkModel = ConnectWithinDistance(5.0)
                deployments {
                    deploy(point(0.0, 0.0))
                    deploy(Point(environment, 0.0, 1.0))
                }
            }
        """.trimIndent()
        val path = Files.createTempFile("dsl-test-", ".alchemist.kts")
        path.writeText(script)
        val loader = LoadAlchemist.from(path.toFile())
        assertNotNull(loader)
    }

    @Test
    fun loadFromFile() {
        val dslUrl = requireNotNull(this.javaClass.getResource("/dsl/kts/15-variables.alchemist.kts")) {
            "Resource /dsl/kts/15-variables.alchemist.kts not found on test classpath"
        }
        val dslFile = File(dslUrl.toURI())
        val dslLoader = { LoadAlchemist.from(dslFile) }
        dslLoader.shouldEqual("dsl/yml/15-variables.yml")
    }

    @Test
    fun loadFromFile2() {
        val dslUrl = requireNotNull(this.javaClass.getResource("/dsl/kts/14-exporters.alchemist.kts")) {
            "Resource /dsl/kts/14-exporters.alchemist.kts not found on test classpath"
        }
        val dslFile = File(dslUrl.toURI())
        val dslLoader = { LoadAlchemist.from(dslFile) }
        dslLoader.shouldEqual("dsl/yml/14-exporters.yml")
    }

    @Test
    fun loadFromFile3() {
        val dslUrl = requireNotNull(this.javaClass.getResource("/dsl/kts/18-properties.alchemist.kts")) {
            "Resource /dsl/kts/18-properties.alchemist.kts not found on test classpath"
        }
        val dslFile = File(dslUrl.toURI())
        val dslLoader = { LoadAlchemist.from(dslFile) }
        dslLoader.shouldEqual("dsl/yml/18-properties.yml")
    }

    @Test
    fun testUrlLoader() {
        val dslUrl = requireNotNull(this.javaClass.getResource("/dsl/kts/12-layers.alchemist.kts")) {
            "Resource /dsl/kts/12-layers.alchemist.kts not found on test classpath"
        }
        val dslLoader = { LoadAlchemist.from(dslUrl) }
        dslLoader.shouldEqual("dsl/yml/12-layers.yml")
    }
}
