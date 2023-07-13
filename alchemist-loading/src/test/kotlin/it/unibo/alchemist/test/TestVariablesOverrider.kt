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
import it.unibo.alchemist.loader.VariablesOverrider
import it.unibo.alchemist.loader.providers.YamlProvider
import org.kaikikm.threadresloader.ResourceLoader

class TestVariablesOverrider : StringSpec({

    "test overrides" {
        val resource = ResourceLoader.getResource("override-test.yml")
        val variables = YamlProvider.from(resource)
        val overrides = listOf(
            "__ignored.x=bar",
            "_test.str=test",
            "_test.int=10",
            "_test.dbl=10.1",
            "_test.strL=[test1, test2]",
            "_test.intL=[9, 19]",
            "_test.dblL=[9.1, 9.87]",
            "_test.arr[0].nst1-1=test",
            "_test.arr[0].nst1-2=test",
            "_test.arr[1].nst2-1[0].nst2-1-1=test",
        )
        val expected = mapOf(
            "foo" to "bar",
            "__ignored" to mapOf(
                "x" to "foo",
            ),
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
            ),
        )

        val result = variables.let { VariablesOverrider.applyOverrides(it, overrides) }

        result shouldBe expected
    }
})
