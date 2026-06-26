/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

class TestCdsApiRc : StringSpec({

    val tempDir: Path = Files.createTempDirectory("cdsapirc-test")

    // a realistic ECMWF unified access token
    val realisticKey = "5b65k8c5-fr34-81dc-82b3-88e1hib45559"

    // deletes the test file after every unit test
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    /**
     *  Writes [content] to a fresh `.cdsapirc`-style file and returns its path.
     */
    fun rcFile(content: String): Path = Files.createTempFile(
        tempDir,
        "rc",
        null,
    ).apply {
        Files.writeString(this, content)
    }

    "reads the token from a well-formed file" {
        val path = rcFile(
            """
            url: https://cds.climate.copernicus.eu/api
            key: $realisticKey
            """.trimIndent(),
        )
        CdsApiRc.readToken(path) shouldBe realisticKey
    }

    "splits on the FIRST colon, preserving colons inside the token" {
        /*
         * the token exact format may change in the future:
         * non-UUID token with internal ':' must survive intact.
         */
        val futureKey = "aaa:bbb:ccc"
        val path = rcFile("key: $futureKey")
        CdsApiRc.readToken(path) shouldBe futureKey
    }

    "trims surrounding whitespace around the token value" {
        // ECMWF writes a space after the colon in the user-guide
        val path = rcFile("key:    $realisticKey   ")
        CdsApiRc.readToken(path) shouldBe realisticKey
    }

    "ignores the url line and any other keys" {
        val path = rcFile(
            """
            url: https://ewds.climate.copernicus.eu/api
            verify: 0
            key: $realisticKey
            other: another property
            """.trimIndent(),
        )
        CdsApiRc.readToken(path) shouldBe realisticKey
    }

    "ignores comment lines" {
        val path = rcFile(
            """
            # personal access token below
            key: $realisticKey
            # personal access token above
            """.trimIndent(),
        )
        CdsApiRc.readToken(path) shouldBe realisticKey
    }

    "throws IllegalStateException when no key line is present" {
        val path = rcFile("url: https://cds.climate.copernicus.eu/api")
        shouldThrow<IllegalStateException> { CdsApiRc.readToken(path) }
    }
})
