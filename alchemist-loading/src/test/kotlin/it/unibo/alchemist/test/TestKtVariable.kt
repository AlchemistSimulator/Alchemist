/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.testsupport.loadAlchemist
import it.unibo.alchemist.testsupport.loadAlchemistFromResource
import it.unibo.alchemist.util.ClassPathScanner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader
import javax.script.ScriptException

class TestKtVariable<T, P : Position<P>> : StringSpec({
    "test loading a kotlin variable" {
        val file = ResourceLoader.getResource("testktvar.yml")
        assertNotNull(file)
        val loader = LoadAlchemist.from(file)
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
    ClassPathScanner.resourcesMatching(".*", "regression/should-fail/kt-script").forEach { spec ->
        "test syntax errors in ${spec.file}" {
            val exception = shouldThrow<RuntimeException> {
                LoadAlchemist.from(spec).getDefault<Any, Nothing>()
            }
            val exceptions = generateSequence(exception, Throwable::cause).run {
                // Avoid circular causes
                val accumulator = mutableSetOf<Throwable>()
                takeWhile { it !in accumulator }.onEach(accumulator::add)
            }
            exceptions.find { it is ScriptException } shouldNot beNull()
        }
    }
    "test 'type' keyword clashes" {
        loadAlchemist<Any, Nothing>("regression/2022-coordination-type-clash.yml") shouldNot beNull()
    }
    "test null values in bindings" {
        val simulation = loadAlchemistFromResource("regression/2022-coordination-null-bindings.yml")
        simulation shouldNot beNull()
        val variable = simulation.variables["result"]
        variable shouldNot beNull()
        val values = variable?.toList().orEmpty()
        values.forEach { it shouldBe "null" }
    }
})
