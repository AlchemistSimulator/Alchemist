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
import it.unibo.alchemist.boundary.loader.Overrides.overrideAll
import it.unibo.alchemist.boundary.modelproviders.YamlProvider
import org.kaikikm.threadresloader.ResourceLoader

class TestVariablesOverrider : StringSpec({

    "overriding variables in a YAML file should be possible" {
        val resource = ResourceLoader.getResource("override/testOverride.yml")
        val variables = YamlProvider.from(resource)

        val resource1 = ResourceLoader.getResource("override/testOverride1.yml")
        val override1 = resource1.readText()
        val resource2 = ResourceLoader.getResource("override/testOverride2.yml")
        val override2 = resource2.readText()

        val overrides = listOf(override1, override2)
        val resourceExpected = ResourceLoader.getResource("override/testOverrideResult.yml")
        val expected = YamlProvider.from(resourceExpected)

        val result = variables.overrideAll(overrides)

        result shouldBe expected
    }
})
