/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition.utility

import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Size of the buffer used to stream a file through the MD5 digest, in bytes.
 */
private const val DIGEST_BUFFER_BYTES = 8 * 1024 // 8 KB

/**
 * Number of hexadecimal digits in a well-formed MD5 digest.
 */
private const val MD5_HEX_DIGITS = 32

/**
 * Verifies the integrity of a downloaded [file] against the metadata the data store advertised for
 * it: the byte count must equal [expectedSizeBytes] and, when [expectedMd5] is supplied,
 * the MD5 digest must equal it.
 *
 * **Note on the advertised MD5:** the data store emits hexadecimal hashes **without left
 * zero-padding**, so a digest such as `0aa45b...` is advertised as the 31-character
 * `aa45b...`. The advertised value is therefore left-padded back to [MD5_HEX_DIGITS] before comparison.
 *
 * **Note for archives**: for an archive (e.g. ZIP) the advertised size and digest describe the
 * archive itself, not its extracted entries.
 *
 * @param file the downloaded file to check.
 * @param expectedSizeBytes the expected size in bytes.
 * @param expectedMd5 the expected MD5 digest as a hex string, if advertised.
 * @param checksumUnusableAction an action performed with the advertised checksum and filename
 * if the checksum turns out to be unusable.
 *
 * @throws IllegalStateException if the actual size, or the actual MD5, does not match.
 */
internal fun verify(
    file: Path,
    expectedSizeBytes: Long,
    expectedMd5: String? = null,
    checksumUnusableAction: (String, String) -> Unit = { _, _ -> },
) {
    val actualSize = Files.size(file)

    check(actualSize == expectedSizeBytes) {
        "Size mismatch for '${file.fileName}': expected $expectedSizeBytes bytes, got $actualSize"
    }

    // no checksum advertised: nothing to verify.
    val advertised = expectedMd5 ?: return
    val expected = advertised.lowercase().padStart(MD5_HEX_DIGITS, '0')

    // warns the user that no md5 was provided.
    if (!expected.isMd5Hex()) {
        checksumUnusableAction(advertised, file.fileName.toString())
        return
    }

    val actual = md5Hex(file)

    check(actual == expected) {
        "MD5 mismatch for '${file.fileName}': expected $expected " +
            "(advertised as '$advertised'), got $actual"
    }
}

/**
 * @return `true` if this string is a well-formed lowercase MD5 hex digest.
 */
private fun String.isMd5Hex(): Boolean = length == MD5_HEX_DIGITS && all { it in '0'..'9' || it in 'a'..'f' }

/**
 * Computes the MD5 digest of [file] as a lowercase hex string,
 * streaming the file through the digest so arbitrarily large files
 * are never fully held in memory.
 *
 * @param file the file to digest.
 * @return the MD5 digest, as a lowercase hex string.
 */
internal fun md5Hex(file: Path): String {
    val digest = MessageDigest.getInstance("MD5")
    DigestInputStream(Files.newInputStream(file), digest).use { stream ->
        // at any given time, a maximum of DIGEST_BUFFER_BYTES bytes are allocated in memory.
        val buffer = ByteArray(DIGEST_BUFFER_BYTES)
        // fills the buffer on every call until there are no more bytes available.
        while (stream.read(buffer) != -1) {
            // reading feeds the digest. The bytes themselves are discarded.
        }
    }
    return digest.digest().toHexString()
}
