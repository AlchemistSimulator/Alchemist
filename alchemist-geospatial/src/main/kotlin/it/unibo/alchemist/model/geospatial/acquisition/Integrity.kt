/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.nio.file.Files
import java.nio.file.Path
import java.security.DigestInputStream
import java.security.MessageDigest

/**
 * Size of the buffer used to stream a file through the MD5 digest, in bytes.
 */
private const val DIGEST_BUFFER_BYTES = 8 * 1024 // 8 KB

/**
 * Verifies the integrity of a downloaded [file] against the metadata the data store
 * advertised for it: the byte count must equal [expectedSizeBytes] and the MD5 digest must equal
 * [expectedMd5] (compared case-insensitively).
 *
 * **Note for archives**: if the downloaded file is an archive (e.g., ZIP), the byte count
 * and MD5 hash typically refer to the archive itself and not to its extracted files.
 *
 * @param file the downloaded file to check.
 * @param expectedSizeBytes the expected size in bytes.
 * @param expectedMd5 the expected MD5 digest as a hex string.
 *
 * @throws IllegalStateException if the actual size or MD5 does not match the expected value.
 */
internal fun verify(file: Path, expectedSizeBytes: Long, expectedMd5: String) {
    val actualSize = Files.size(file)
    check(actualSize == expectedSizeBytes) {
        "Size mismatch for '${file.fileName}': expected $expectedSizeBytes bytes, got $actualSize"
    }
    val actualMd5 = md5Hex(file)
    check(actualMd5.equals(expectedMd5, ignoreCase = true)) {
        "MD5 mismatch for '${file.fileName}': expected $expectedMd5, got $actualMd5"
    }
}

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
    return digest.digest().joinToString("") {
        /*
         * 0 = padding with zeros instead of spaces.
         * 2 = at least two digits.
         * x = all hex are represented in lowercase.
         */
        "%02x".format(it)
    }
}
