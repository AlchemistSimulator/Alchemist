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
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TestArchives : StringSpec({

    val tempDir: Path = Files.createTempDirectory("archives-test")

    // deletes the directory and its files after the tests
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    // the subdir for each test
    lateinit var dir: Path
    beforeTest {
        dir = Files.createTempDirectory(tempDir, "dir")
    }

    /**
     * Writes a real zip named [zipName] into [dir] with the given entry path -> content.
     */
    fun writeZip(dir: Path, zipName: String, entries: Map<String, String>): Path {
        val zipPath = dir.resolve(zipName)
        ZipOutputStream(Files.newOutputStream(zipPath)).use { zos ->
            entries.forEach { (name, content) ->
                zos.putNextEntry(ZipEntry(name))
                zos.write(content.toByteArray())
                zos.closeEntry()
            }
        }
        return zipPath
    }

    /**
     * Returns the set of regular file basenames in [dir].
     */
    fun fileNames(dir: Path): Set<String> = Files.list(dir).use { s ->
        s.filter { Files.isRegularFile(it) }
            .map { it.fileName.toString() }.toList()
    }.toSet()

    "a non-zip file is left untouched" {
        val data = dir.resolve("dis24.nc")
        Files.writeString(data, "I'm not a ZIP!")
        val before = Files.readString(data)

        flattenArchives(dir)

        data.shouldExist()
        Files.readString(data) shouldBe before
        fileNames(dir) shouldBe setOf("dis24.nc")
    }

    "a single-entry zip is extracted flat and the archive is deleted" {
        val zip = writeZip(dir, "result.zip", mapOf("dis24.nc" to "payload"))

        flattenArchives(dir)

        zip.shouldNotExist()
        dir.resolve("dis24.nc").shouldExist()
        Files.readString(dir.resolve("dis24.nc")) shouldBe "payload"
        fileNames(dir) shouldBe setOf("dis24.nc")
    }

    "multiple archives in the same dir are all extracted and deleted" {
        writeZip(dir, "part1.zip", mapOf("a.nc" to "A"))
        writeZip(dir, "part2.zip", mapOf("b.nc" to "B"))

        flattenArchives(dir)

        fileNames(dir) shouldBe setOf("a.nc", "b.nc")
    }

    "a data file alongside an archive: the file stays, the archive is flattened" {
        Files.writeString(dir.resolve("already.nc"), "plain")
        writeZip(dir, "result.zip", mapOf("fromzip.nc" to "Z"))

        flattenArchives(dir)

        fileNames(dir) shouldBe setOf("already.nc", "fromzip.nc")
    }

    "a multi-entry zip extracts every entry" {
        writeZip(dir, "result.zip", mapOf("a.nc" to "A", "b.nc" to "B"))

        flattenArchives(dir)

        fileNames(dir) shouldBe setOf("a.nc", "b.nc")
        Files.readString(dir.resolve("a.nc")) shouldBe "A"
        Files.readString(dir.resolve("b.nc")) shouldBe "B"
    }

    "nested entry paths are flattened to their basename" {
        writeZip(dir, "result.zip", mapOf("data/2024/06/dis24.nc" to "deep"))

        flattenArchives(dir)

        dir.resolve("dis24.nc").shouldExist()
        Files.readString(dir.resolve("dis24.nc")) shouldBe "deep"
        fileNames(dir) shouldBe setOf("dis24.nc")
    }

    "detection is by content, not extension: a zip named '.nc' is still extracted" {
        // the archive itself is named like a data file
        val zip = writeZip(dir, "payload.nc", mapOf("dis24.nc" to "inner"))

        flattenArchives(dir)

        zip.shouldNotExist()
        dir.resolve("dis24.nc").shouldExist()
        Files.readString(dir.resolve("dis24.nc")) shouldBe "inner"
    }

    "detection is by content, not extension: a non-zip named '.zip' is left untouched" {
        val fake = dir.resolve("archive.zip")
        Files.writeString(fake, "this is plain text, not a zip")

        flattenArchives(dir)

        fake.shouldExist()
        Files.readString(fake) shouldBe "this is plain text, not a zip"
    }

    "a flatten collision (two entries, same basename) throws IllegalStateException" {
        writeZip(dir, "result.zip", mapOf("regionA/dis24.nc" to "A", "regionB/dis24.nc" to "B"))

        shouldThrow<IllegalStateException> { flattenArchives(dir) }
    }

    "an empty zip (with no entries) extracts nothing, is deleted, and leaves the dir empty" {
        val zip = writeZip(dir, "result.zip", emptyMap())

        flattenArchives(dir)

        zip.shouldNotExist()
        fileNames(dir) shouldBe emptySet()
    }
})
