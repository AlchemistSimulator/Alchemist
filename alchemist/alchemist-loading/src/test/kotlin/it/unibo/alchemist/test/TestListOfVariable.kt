/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotlintest.matchers.maps.shouldContainKey
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.loader.YamlLoader
import java.util.stream.Collectors
import org.apache.commons.codec.Resources

class TestListOfVariable : StringSpec({
    "Test YAML loading" {
        val loader = YamlLoader(Resources.getInputStream("testListOfVariable.yml"))
        loader.variables shouldContainKey "var"
        loader.variables["var"]!!.stream().collect(Collectors.toList<Any>()) shouldBe listOf(1, 2, 3, 4)
    }
})
