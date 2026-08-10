/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Path

class TestPaths : StringSpec({

    // expandUser function test
    val homeProperty: String = System.getProperty("user.home")
    val home: Path = Path.of(homeProperty)
    val childName = "geospatial-cache"
    val grandChildName = "glofas"

    "a lone tilde expands to the user home" {
        "~".expandUser() shouldBe home
    }

    "a tilde followed by a slash and a single segment expands under the user home" {
        "~/$childName".expandUser() shouldBe Path.of(homeProperty, childName)
    }

    "a tilde followed by a backslash and a single segment expands under the user home" {
        "~\\$childName".expandUser() shouldBe Path.of(homeProperty, childName)
    }

    "a tilde followed by several segments expands preserving all the subdirectories" {
        "~/$childName/$grandChildName".expandUser() shouldBe Path.of(homeProperty, childName, grandChildName)
    }

    "a trailing separator after the tilde expands to the user home" {
        "~/".expandUser() shouldBe home
    }

    "paths that do not start with a tilde are converted as-is" {
        val untouched = listOf(
            "$childName/$grandChildName",
            "./$childName",
            "../$childName",
            "",
            "/tmp/~$childName",
            "$childName~",
        )
        untouched.forEach { path ->
            withClue("path: '$path'") {
                path.expandUser() shouldBe Path.of(path)
            }
        }
    }

    "an absolute path is left untouched" {
        val absolute = home.resolve(childName).toAbsolutePath().toString()
        absolute.expandUser() shouldBe Path.of(absolute)
    }

    "the expansion does not normalize the path" {
        val notNormalized = "~/$childName/../$grandChildName".expandUser()
        withClue("normalization is out of contract: '..' must survive") {
            notNormalized shouldNotBe notNormalized.normalize()
        }
    }

    "the expansion does not require the path to exist" {
        // current time just to be sure the dir does not exist
        val missing = "~/$childName-does-not-exist-${System.nanoTime()}".expandUser()
        withClue("expansion is a pure string-to-Path operation") {
            missing.startsWith(home) shouldBe true
        }
    }
})
