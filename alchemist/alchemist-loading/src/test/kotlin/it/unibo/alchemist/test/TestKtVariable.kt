/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotlintest.specs.StringSpec
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.interfaces.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader

class TestKtVariable<T, P : Position<P>> : StringSpec({
    "test loading a kotlin variable" {
        val file = ResourceLoader.getResourceAsStream("testktvar.yml")
        assertNotNull(file)
        val loader = YamlLoader(file)
        assertNotNull(loader.getWith<T, P>(emptyMap<String, String>()))
        loader.constants.let { variable ->
            assertEquals(23, variable["a"])
            val expectedTest2 = listOf("a", 5.5)
            assertEquals(expectedTest2, variable["test2"])
            val expectedTest = listOf(23, 5.5)
            assertEquals(expectedTest, variable["test"])
            assertEquals(expectedTest + expectedTest2, variable["test3"])
        }
    }
})
