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
import java.util.zip.ZipException
import java.util.zip.ZipFile

/**
 * Extract all ZIP files inside of [dir] and deletes them.
 *
 * Each regular file in [dir] that can be opened as a valid ZIP archive is **extracted flat** (each
 * entry by its basename) into [dir], and the archive itself is then deleted. Files that are not ZIP
 * archives are left untouched.
 *
 * **Note:** it is the caller's responsibility to check, if necessary, whether [dir] is empty after this
 * operation, as empty ZIPs are extracted too.
 *
 * @param dir the directory that contains the ZIP archives to extract.
 *
 * @throws IllegalStateException if two entries in an archive collapse to the same basename.
 */
internal fun flattenArchives(dir: Path) {
    val files = Files.list(dir).use {
        it.filter(Files::isRegularFile).toList()
    }
    for (file in files) {
        // extract the ZIP file (if it is one) and deletes it.
        if (extractArchive(file, dir)) {
            Files.delete(file)
        }
    }
}

/**
 * Extracts [archive] flat into [dir] if it is a ZIP.
 *
 * @return `true` if [archive] was a valid ZIP (its entries were extracted), `false` if it is not a
 * ZIP and was left untouched.
 * @throws IllegalStateException on a basename collision between two entries.
 */
private fun extractArchive(archive: Path, dir: Path): Boolean = try {
    ZipFile(archive.toFile()).use { zip ->
        for (entry in zip.entries()) {
            // nothing to do if the entry is directory.
            if (entry.isDirectory) continue

            // it's a file. Resolves the new path.
            val target = dir.resolve(Path.of(entry.name).fileName.toString())
            // a file with the exact same path already exists.
            check(Files.notExists(target)) {
                "Flatten collision on '${target.fileName}' from '${archive.fileName}'"
            }

            // copies the content in the target path
            zip.getInputStream(entry).use { source ->
                Files.copy(source, target)
            }
        }
    }
    true
} catch (_: ZipException) {
    // the file was not a zip archive.
    false
}
