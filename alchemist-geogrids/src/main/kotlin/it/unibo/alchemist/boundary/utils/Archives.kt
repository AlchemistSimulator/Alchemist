/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import java.nio.file.Path
import okio.ByteString.Companion.decodeHex
import okio.FileSystem
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.buffer
import okio.openZip

/**
 * Leading bytes of a ZIP archive: a local file header, or the end of central directory record of an empty archive.
 */
private val zipSignatures = listOf("504b0304", "504b0506").map { it.decodeHex() }

/**
 * Extract all ZIP files inside of [dir] and deletes them.
 *
 * Each regular file in [dir] starting with a ZIP signature is **extracted flat** (each
 * entry by its basename) into [dir], and the archive itself is then deleted. Files that are not ZIP
 * archives are left untouched.
 *
 * **Note:** it is the caller's responsibility to check, if necessary, whether [dir] is empty after this
 * operation, as empty ZIPs are extracted too.
 *
 * @param dir the directory that contains the ZIP archives to extract.
 * @throws IllegalStateException if two entries in an archive collapse to the same basename.
 */
internal fun flattenArchives(dir: Path) {
    val directory = dir.toOkioPath()
    FileSystem.SYSTEM.list(directory)
        .filter { FileSystem.SYSTEM.metadata(it).isRegularFile && it.isZip() }
        .forEach { archive ->
            extractArchive(archive, directory)
            FileSystem.SYSTEM.delete(archive)
        }
}

/**
 * Extracts the ZIP [archive] flat into [dir].
 *
 * @throws IllegalStateException on a basename collision between two entries.
 */
private fun extractArchive(archive: okio.Path, dir: okio.Path) {
    FileSystem.SYSTEM.openZip(archive).use { zip ->
        zip.listRecursively("/".toPath())
            .filter { zip.metadata(it).isRegularFile }
            .forEach { entry ->
                val target = dir / entry.name
                check(!FileSystem.SYSTEM.exists(target)) {
                    "Flatten collision on '${target.name}' from '${archive.name}'"
                }
                FileSystem.SYSTEM.write(target, mustCreate = true) {
                    zip.source(entry).use { writeAll(it) }
                }
            }
    }
}

/**
 * Whether this file starts with a ZIP signature: detection is by content, not by extension.
 */
private fun okio.Path.isZip(): Boolean = FileSystem.SYSTEM.source(this).buffer().use { source ->
    zipSignatures.any { source.rangeEquals(0, it) }
}
