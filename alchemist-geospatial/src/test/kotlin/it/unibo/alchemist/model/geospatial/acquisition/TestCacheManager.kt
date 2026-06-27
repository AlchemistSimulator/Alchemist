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

class TestCacheManager : StringSpec({

    val tempDir: Path = Files.createTempDirectory("cache-manager-test")

    // the name of the created data file
    val dataFileName = "data.nc"

    // a lambda that writes a single file in the directory
    val writeOneFile: (Path) -> Unit = { dir ->
        Files.writeString(dir.resolve(dataFileName), "payload")
    }

    // deletes the directory and its files after the tests
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    /**
     * Create a fresh new unique root.
     */
    fun freshRoot(): Path = Files.createTempDirectory(tempDir, "root")

    /**
     * A [CacheKey] that returns a fixed directory name.
     */
    fun key(name: String): CacheKey = object : CacheKey {
        override fun toFileName(): String = name
    }

    "miss: produce runs exactly once and its files are promoted" {
        val cache = CacheManager(freshRoot())
        var calls = 0
        val result = cache.getOrProduce(key("entry_a")) { dir ->
            calls++
            writeOneFile(dir)
        }
        calls shouldBe 1
        result.shouldExist()
        result.resolve(dataFileName).shouldExist()
    }

    "hit: the second call reuses the first entry without re-producing" {
        val cache = CacheManager(freshRoot())
        val first = cache.getOrProduce(key("entry_b")) { dir ->
            Files.writeString(dir.resolve(dataFileName), "first")
        }
        // if the cache wrongly re-ran produce, the file would contain "SECOND"
        val second = cache.getOrProduce(key("entry_b")) { dir ->
            Files.writeString(dir.resolve(dataFileName), "SECOND")
        }
        second shouldBe first
        Files.readString(second.resolve(dataFileName)) shouldBe "first"
    }

    "the returned directory name is exactly key.toFileName() under root" {
        val root = freshRoot()
        val cache = CacheManager(root)
        val dirName = "cems-glofas_abc123"
        val result = cache.getOrProduce(key(dirName), writeOneFile)
        result shouldBe root.resolve(dirName)
    }

    "produce failure: the exception propagates and no entry is promoted" {
        val root = freshRoot()
        val cache = CacheManager(root)
        shouldThrow<IllegalStateException> {
            cache.getOrProduce(key("entry_fail")) { error("download blew up") }
        }
        root.resolve("entry_fail").shouldNotExist() // no poisoned entry
    }

    "produce failure: the temporary directory is cleaned up, leaving .tmp empty" {
        val root = freshRoot()
        val cache = CacheManager(root)
        // ignores the exception
        runCatching {
            cache.getOrProduce(key("entry_fail2")) { dir ->
                Files.writeString(dir.resolve("partial.nc"), "half")
                // emulates something gone wrong on file writing
                error("error after writing")
            }
        }
        // .tmp must hold no leftover temp dirs
        val tmpRoot = root.resolve(".tmp")
        val leftovers = Files.list(tmpRoot).use { it.toList() }
        leftovers.size shouldBe 0
    }

    "empty result: produce leaves no file and throws IllegalStateException, nothing promoted" {
        val root = freshRoot()
        val cache = CacheManager(root)
        val entryName = "entry_empy"
        shouldThrow<IllegalStateException> {
            cache.getOrProduce(key(entryName)) { /* writes nothing */ }
        }
        root.resolve(entryName).shouldNotExist()
    }

    "validate before promoting: a dir with only subdirs (no regular file) is rejected" {
        val root = freshRoot()
        val cache = CacheManager(root)
        val entryName = "entry_subdir"
        shouldThrow<IllegalStateException> {
            cache.getOrProduce(key(entryName)) { dir ->
                // a directory, but no regular file
                Files.createDirectory(dir.resolve("nested"))
            }
        }
        root.resolve(entryName).shouldNotExist()
    }

    "race lost in promote: a peer wins after the fast-path check; peer reused, local discarded, temp cleaned" {
        val root = freshRoot()
        val cache = CacheManager(root)
        val k = key("entry_race_real")
        val finalDir = root.resolve(k.toFileName())

        val result = cache.getOrProduce(k) { dir ->
            Files.writeString(dir.resolve("local.nc"), "loser")
            // a peer produces the same entry
            Files.createDirectories(finalDir)
            Files.writeString(finalDir.resolve("peer.nc"), "winner")
        }

        result shouldBe finalDir
        result.resolve("peer.nc").shouldExist()
        result.resolve("local.nc").shouldNotExist()
        // local temp should not be considered
        Files.list(root.resolve(".tmp")).use { it.toList() } shouldBe emptyList()
    }

    "hit is detected even across a fresh CacheManager over the same root" {
        val root = freshRoot()
        CacheManager(root).getOrProduce(key("entry_persist")) { dir ->
            Files.writeString(dir.resolve(dataFileName), "first")
        }
        // a brand-new manager instance over the same root must reuse the existing entry
        val result = CacheManager(root).getOrProduce(key("entry_persist")) { dir ->
            Files.writeString(dir.resolve(dataFileName), "SECOND")
        }
        Files.readString(result.resolve(dataFileName)) shouldBe "first"
    }
})
