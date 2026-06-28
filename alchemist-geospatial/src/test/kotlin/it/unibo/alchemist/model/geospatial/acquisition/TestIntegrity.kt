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

class TestIntegrity : StringSpec({

    val tempDir: Path = Files.createTempDirectory("integrity-test")
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    lateinit var file: Path
    beforeTest {
        file = Files.createTempFile(tempDir, "asset", ".bin")
    }

    // some exact MD5 digests
    val knownMD5 = mapOf(
        "" to "d41d8cd98f00b204e9800998ecf8427e",
        "abc" to "900150983cd24fb0d6963f7d28e17f72",
    )

    "md5Hex of empty file matches the known digest" {
        // file is created empty by beforeTest
        md5Hex(file) shouldBe knownMD5[""]
    }

    "md5Hex of 'abc' matches the known digest" {
        Files.writeString(file, "abc")
        md5Hex(file) shouldBe knownMD5["abc"]
    }

    "md5Hex is always 32 lowercase hex chars" {
        Files.writeString(file, "whatever content")
        val hex = md5Hex(file)
        hex.length shouldBe 32
        hex shouldBe hex.lowercase()
    }

    "verify passes when size and MD5 both match" {
        Files.writeString(file, "abc")
        // 3 bytes, known MD5
        verify(file, 3, knownMD5["abc"]!!)
    }

    "verify accepts uppercase expected MD5 (case-insensitive)" {
        Files.writeString(file, "abc")
        verify(file, 3, knownMD5["abc"]!!.uppercase())
    }

    "verify throws on size mismatch" {
        Files.writeString(file, "abc")
        shouldThrow<IllegalStateException> {
            verify(file, 999)
        }
    }

    "verify throws on MD5 mismatch even when size is right" {
        Files.writeString(file, "abc")
        shouldThrow<IllegalStateException> {
            // correct size (3) but wrong checksum
            verify(file, 3, "00000000000000000000000000000000")
        }
    }
})
