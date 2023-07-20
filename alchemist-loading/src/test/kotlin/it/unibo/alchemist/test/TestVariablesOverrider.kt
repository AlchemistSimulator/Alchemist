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
import it.unibo.alchemist.boundary.modelproviders.YamlProvider
import it.unibo.alchemist.model.VariablesOverrider
import org.kaikikm.threadresloader.ResourceLoader

class TestVariablesOverrider : StringSpec({

    "test overrides" {
        val resource = ResourceLoader.getResource("override/testOverride.yml")
        val variables = YamlProvider.from(resource)

        val resource1 = ResourceLoader.getResource("override/testOverride1.yml")
        val override1 = resource1.readText()
        val resource2 = ResourceLoader.getResource("override/testOverride2.yml")
        val override2 = resource2.readText()

        val overrides = listOf(override1, override2)
        val expected = mapOf(
            "foo" to "bar",
            "_test" to mapOf(
                "str" to "test",
                "int" to 10,
                "dbl" to 10.1,
                "strL" to listOf("test1", "test2"),
                "intL" to listOf(9, 19),
                "dblL" to listOf(9.1, 9.87),
                "arr" to listOf(
                    mapOf(
                        "nst1-1" to "test",
                        "nst1-2" to "test",
                    ),
                    mapOf(
                        "nst2-1" to listOf(
                            mapOf(
                                "nst2-1-1" to "test",
                            ),
                        ),
                    ),
                ),
                "map" to mapOf(
                    "elem1" to 10,
                    "elem2" to mapOf(
                        "key1" to "testtest",
                        "key2" to mapOf(
                            "foo" to listOf(
                                10,
                            ),
                        ),
                    ),
                ),
            ),
        )

        val result = variables.let { VariablesOverrider.applyOverrides(it, overrides) }

        result shouldBe expected
    }
})
