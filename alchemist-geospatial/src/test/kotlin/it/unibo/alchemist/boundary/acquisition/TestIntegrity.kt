/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.acquisition.utility.md5Hex
import it.unibo.alchemist.boundary.acquisition.utility.verify
import java.nio.file.Files
import java.nio.file.Path

class TestIntegrity : StringSpec({

    val tempDir: Path = Files.createTempDirectory("integrity-test")

    lateinit var file: Path

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    beforeTest {
        file = Files.createTempFile(tempDir, "asset", ".bin")
    }

    /**
     * Exact MD5 digests.
     */
    val knownMD5 = mapOf(
        "" to "d41d8cd98f00b204e9800998ecf8427e",
        "abc" to "900150983cd24fb0d6963f7d28e17f72",
        "a" to "0cc175b9c0f1b6a831c399e269772661",
        "168" to "006f52e9102a8d3be2fe5614f42ba989",
    )

    /**
     * [knownMD5], but formatted the way data store advertises them:
     * hashes are emitted without left zero-padding.
     */
    val advertisedUnpadded = mapOf(
        "a" to "cc175b9c0f1b6a831c399e269772661", // 31 chars
        "168" to "6f52e9102a8d3be2fe5614f42ba989", // 30 chars
    )

    "md5Hex of empty file matches the known digest" {
        // file is created empty by beforeTest
        md5Hex(file) shouldBe knownMD5[""]
    }

    knownMD5.forEach { (content, digest) ->
        "md5Hex of $content matches the known digest" {
            Files.writeString(file, content)
            md5Hex(file) shouldBe digest
        }
    }

    "md5Hex preserves the leading zero nibbles of a digest" {
        advertisedUnpadded.keys.forEach { content ->
            withClue("content: '$content'") {
                Files.writeString(file, content)
                md5Hex(file) shouldBe knownMD5.getValue(content)
            }
        }
    }

    "md5Hex is always 32 lowercase hex chars" {
        knownMD5.keys.forEach { content ->
            withClue("content: '$content'") {
                Files.writeString(file, content)
                val hex = md5Hex(file)
                hex.length shouldBe 32
                hex shouldBe hex.lowercase()
            }
        }
    }

    "verify passes when size and MD5 both match" {
        Files.writeString(file, "abc")
        // 3 bytes, known MD5
        verify(file, 3, knownMD5.getValue("abc"))
    }

    "verify passes when no MD5 is advertised" {
        Files.writeString(file, "abc")
        verify(file, 3)
    }

    "verify accepts uppercase expected MD5 (case-insensitive)" {
        Files.writeString(file, "abc")
        verify(file, 3, knownMD5.getValue("abc").uppercase())
    }

    /*
     * Data stores strip leading zeros from the digest it advertises, so
     * a non-32 chars digest can be valid.
     */
    "verify accepts an MD5 advertised without its leading zeros" {
        Files.writeString(file, "a")
        verify(file, 1, advertisedUnpadded.getValue("a"))
    }

    "verify accepts an MD5 advertised without two leading zeros" {
        Files.writeString(file, "168")
        verify(file, 3, advertisedUnpadded.getValue("168"))
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
            verify(file, 3, "ffffffffffffffffffffffffffffffff")
        }
    }

    "verify throws on a short MD5 that is wrong rather than unpadded" {
        Files.writeString(file, "a")
        shouldThrow<IllegalStateException> {
            // unpadded digest (of "a") with its last character altered
            verify(file, 1, "cc175b9c0f1b6a831c399e26977266f")
        }
    }

    "verify skips an unusable MD5 instead of failing" {
        Files.writeString(file, "abc")
        verify(file, 3, "sha256:not-a-digest")
    }

    "verify still enforces the size when the MD5 is unusable" {
        Files.writeString(file, "abc")
        shouldThrow<IllegalStateException> {
            verify(file, 999, "not-a-digest")
        }
    }
})
