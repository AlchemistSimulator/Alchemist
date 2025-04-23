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
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.variables.SystemEnvVariable
import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.kaikikm.threadresloader.ResourceLoader

class TestEnvVariables<T, P : Position<P>> :
    StringSpec({
        val resourceFile = "testEnvVariables.yml"

        fun verifyEnvironmentVariable(environment: Map<String, String>, variableName: String, expectedValue: Any) {
            mockkObject(SystemEnvVariable.Companion)
            environment.forEach { (key, value) ->
                every { SystemEnvVariable.Companion.loadFromEnv(key) } returns value
            }
            val file = ResourceLoader.getResource(resourceFile)
            assertNotNull(file)
            val loader = LoadAlchemist.from(file)
            assertNotNull(loader.getWith<T, P>(emptyMap<String, T>()))
            loader.constants[variableName]?.let { constant ->
                assertEquals(expectedValue, constant)
            }
            unmockkObject(SystemEnvVariable.Companion)
        }

        "test alchemist should support system env variables" {
            verifyEnvironmentVariable(
                mapOf("value" to "10", "anyValue" to "foo"),
                "envWithDefault",
                10,
            )
        }

        "test alchemist should load default value when env is not set" {
            verifyEnvironmentVariable(
                mapOf("anyValue" to "foo"),
                "envWithDefault",
                0,
            )
        }

        "test alchemist should load boolean from env" {
            verifyEnvironmentVariable(
                mapOf("anyValue" to "true"),
                "env",
                true,
            )
        }

        "test alchemist should load double from env" {
            verifyEnvironmentVariable(
                mapOf("anyValue" to "10.0"),
                "env",
                10.0,
            )
        }

        "test alchemist should load string from env" {
            verifyEnvironmentVariable(
                mapOf("anyValue" to "hello"),
                "env",
                "hello",
            )
        }

        "test alchemist should throw an exception when an environment value is not set and has not default" {
            val file = ResourceLoader.getResource(resourceFile)
            assertNotNull(file)
            assertThrows<IllegalStateException> {
                val loader = LoadAlchemist.from(file)
                loader.getDefault<T, P>()
            }
        }
    })
