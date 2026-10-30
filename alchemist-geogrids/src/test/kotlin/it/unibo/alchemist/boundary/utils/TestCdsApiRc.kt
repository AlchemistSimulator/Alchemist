/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestCdsApiRc {

    private val tempDir: Path = Files.createTempDirectory("cdsapirc-test")

    // a realistic ECMWF unified access token
    private val realisticKey = "5b65k8c5-fr34-81dc-82b3-88e1hib45559"

    // deletes the directory and its files after the tests
    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    /**
     * Writes [content] to a fresh `.cdsapirc`-style file and returns its path.
     */
    private fun rcFile(content: String): Path = Files.createTempFile(
        tempDir,
        "rc",
        null,
    ).apply {
        Files.writeString(this, content)
    }

    @Test
    fun `reads the token from a well-formed file`() {
        val path = rcFile(
            """
            url: https://cds.climate.copernicus.eu/api
            key: $realisticKey
            """.trimIndent(),
        )
        assertEquals(realisticKey, CdsApiRc.readToken(path))
    }

    @Test
    fun `splits on the FIRST colon, preserving colons inside the token`() {
        /*
         * the token exact format may change in the future:
         * non-UUID token with internal ':' must survive intact.
         */
        val futureKey = "aaa:bbb:ccc"
        val path = rcFile("key: $futureKey")
        assertEquals(futureKey, CdsApiRc.readToken(path))
    }

    @Test
    fun `trims surrounding whitespace around the token value`() {
        // ECMWF writes a space after the colon in the user-guide
        val path = rcFile("key:    $realisticKey   ")
        assertEquals(realisticKey, CdsApiRc.readToken(path))
    }

    @Test
    fun `ignores the url line and any other keys`() {
        val path = rcFile(
            """
            url: https://ewds.climate.copernicus.eu/api
            verify: 0
            key: $realisticKey
            other: another property
            """.trimIndent(),
        )
        assertEquals(realisticKey, CdsApiRc.readToken(path))
    }

    @Test
    fun `ignores comment lines`() {
        val path = rcFile(
            """
            # personal access token below
            key: $realisticKey
            # personal access token above
            """.trimIndent(),
        )
        assertEquals(realisticKey, CdsApiRc.readToken(path))
    }

    @Test
    fun `throws IllegalStateException when no key line is present`() {
        val path = rcFile("url: https://cds.climate.copernicus.eu/api")
        assertFailsWith<IllegalStateException> { CdsApiRc.readToken(path) }
    }
}
