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
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.exists
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestArchives {

    private val tempDir: Path = Files.createTempDirectory("archives-test")
    private val netCdfFileName = "dis24.nc"
    private val zipFileName = "result.zip"

    // the subdir for each test
    private val dir: Path = Files.createTempDirectory(tempDir, "dir")

    // deletes the directory and its files after the tests
    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    /**
     * Writes a real zip named [zipName] into [dir] with the given entry path -> content.
     */
    private fun writeZip(dir: Path, zipName: String, entries: Map<String, String>): Path {
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
    private fun fileNames(dir: Path): Set<String> = Files.list(dir).use { s ->
        s.filter { Files.isRegularFile(it) }
            .map { it.fileName.toString() }.toList()
    }.toSet()

    @Test
    fun `a non-zip file is left untouched`() {
        val data = dir.resolve(netCdfFileName)
        Files.writeString(data, "I'm not a ZIP!")
        val before = Files.readString(data)
        flattenArchives(dir)
        assertTrue(data.exists(), "$data should exist")
        assertEquals(before, Files.readString(data))
        assertEquals(setOf(netCdfFileName), fileNames(dir))
    }

    @Test
    fun `a single-entry zip is extracted flat and the archive is deleted`() {
        val zip = writeZip(dir, zipFileName, mapOf(netCdfFileName to "payload"))
        flattenArchives(dir)
        assertFalse(zip.exists(), "$zip should not exist")
        assertTrue(dir.resolve(netCdfFileName).exists(), "$netCdfFileName should exist in $dir")
        assertEquals("payload", Files.readString(dir.resolve(netCdfFileName)))
        assertEquals(setOf(netCdfFileName), fileNames(dir))
    }

    @Test
    fun `multiple archives in the same dir are all extracted and deleted`() {
        writeZip(dir, "part1.zip", mapOf("a.nc" to "A"))
        writeZip(dir, "part2.zip", mapOf("b.nc" to "B"))
        flattenArchives(dir)
        assertEquals(setOf("a.nc", "b.nc"), fileNames(dir))
    }

    @Test
    fun `a data file alongside an archive stays while the archive is flattened`() {
        Files.writeString(dir.resolve("already.nc"), "plain")
        writeZip(dir, zipFileName, mapOf("fromzip.nc" to "Z"))
        flattenArchives(dir)
        assertEquals(setOf("already.nc", "fromzip.nc"), fileNames(dir))
    }

    @Test
    fun `a multi-entry zip extracts every entry`() {
        writeZip(dir, zipFileName, mapOf("a.nc" to "A", "b.nc" to "B"))
        flattenArchives(dir)
        assertEquals(setOf("a.nc", "b.nc"), fileNames(dir))
        assertEquals("A", Files.readString(dir.resolve("a.nc")))
        assertEquals("B", Files.readString(dir.resolve("b.nc")))
    }

    @Test
    fun `nested entry paths are flattened to their basename`() {
        writeZip(dir, zipFileName, mapOf("data/2024/06/dis24.nc" to "deep"))
        flattenArchives(dir)
        assertTrue(dir.resolve(netCdfFileName).exists(), "$netCdfFileName should exist in $dir")
        assertEquals("deep", Files.readString(dir.resolve(netCdfFileName)))
        assertEquals(setOf(netCdfFileName), fileNames(dir))
    }

    @Test
    fun `detection is by content not extension so a zip named nc is still extracted`() {
        // the archive itself is named like a data file
        val zip = writeZip(dir, "payload.nc", mapOf(netCdfFileName to "inner"))
        flattenArchives(dir)
        assertFalse(zip.exists(), "$zip should not exist")
        assertTrue(dir.resolve(netCdfFileName).exists(), "$netCdfFileName should exist in $dir")
        assertEquals("inner", Files.readString(dir.resolve(netCdfFileName)))
    }

    @Test
    fun `detection is by content not extension so a non-zip named zip is left untouched`() {
        val fake = dir.resolve("archive.zip")
        Files.writeString(fake, "this is plain text, not a zip")
        flattenArchives(dir)
        assertTrue(fake.exists(), "$fake should exist")
        assertEquals("this is plain text, not a zip", Files.readString(fake))
    }

    @Test
    fun `a flatten collision with two entries sharing the same basename throws IllegalStateException`() {
        writeZip(dir, zipFileName, mapOf("regionA/dis24.nc" to "A", "regionB/dis24.nc" to "B"))
        assertFailsWith<IllegalStateException> { flattenArchives(dir) }
    }

    @Test
    fun `an empty zip with no entries extracts nothing, is deleted, and leaves the dir empty`() {
        val zip = writeZip(dir, zipFileName, emptyMap())
        flattenArchives(dir)
        assertFalse(zip.exists(), "$zip should not exist")
        assertEquals(emptySet(), fileNames(dir))
    }
}
