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
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

/**
 * Delegates each fetch call to a configurable [behavior], so that [FileSystemCacheManager] can
 * be tested fully offline.
 */
private class FakeCopernicusProvider(
    private val behavior: (request: CopernicusRequest, targetDir: Path) -> Unit,
) : ExternalDataProvider<CopernicusRequest> {

    var calls: Int = 0
        private set

    override fun fetch(request: CopernicusRequest, targetDir: Path) {
        calls++
        behavior(request, targetDir)
    }
}

class TestFileSystemCacheManager : StringSpec({

    val tempDir: Path = Files.createTempDirectory("cache-manager-test")

    val dataFileName = "data.nc"

    // writes a single file in a directory
    val writeOneFile: (Path) -> Unit = { dir ->
        Files.writeString(dir.resolve(dataFileName), "payload")
    }

    // deletes the directory and its files after the tests
    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    // creates a new root.
    fun newRoot(): Path = Files.createTempDirectory(tempDir, "root")

    /**
     * A minimal [CopernicusRequest] for a test case.
     * The inputs remain constant, only the dataset changes.
     */
    fun request(id: String): CopernicusRequest = CopernicusRequest(dataset = id, inputs = emptyMap())

    "miss: produce runs exactly once and its files are promoted" {
        val req = request("entry_a")
        val provider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val cache = FileSystemCacheManager(provider, newRoot())
        val result = cache.getOrProduce(req)
        provider.calls shouldBe 1
        result.shouldExist()
        result.resolve(dataFileName).shouldExist()
    }

    "hit: the second call reuses the first entry without re-producing" {
        val req = request("entry_b")
        var marker = "first"
        val provider = FakeCopernicusProvider { _, dir -> Files.writeString(dir.resolve(dataFileName), marker) }
        val cache = FileSystemCacheManager(provider, newRoot())
        val first = cache.getOrProduce(req)
        // if the cache manager wrongly re-ran the provider, the file would contain "second"
        marker = "second"
        val second = cache.getOrProduce(req)
        second shouldBe first
        Files.readString(second.resolve(dataFileName)) shouldBe "first"
        provider.calls shouldBe 1
    }

    "the returned directory name is exactly request.toFileName() under root" {
        val root = newRoot()
        val req = request("cems-glofas_abc123")
        val provider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val cache = FileSystemCacheManager(provider, root)
        val result = cache.getOrProduce(req)
        result shouldBe root.resolve(req.toFileName())
    }

    "produce failure: the exception propagates and no entry is promoted" {
        val root = newRoot()
        val req = request("entry_fail")
        val provider = FakeCopernicusProvider { _, _ -> error("download blew up") }
        val cache = FileSystemCacheManager(provider, root)
        shouldThrow<IllegalStateException> { cache.getOrProduce(req) }
        root.resolve(req.toFileName()).shouldNotExist() // no poisoned dir
    }

    "produce failure: the temporary directory is cleaned up, leaving .tmp empty" {
        val root = newRoot()
        val req = request("entry_fail2")
        val provider = FakeCopernicusProvider { _, dir ->
            Files.writeString(dir.resolve("partial.nc"), "half")
            // simulates something gone wrong after having already written a file
            error("error after writing")
        }
        val cache = FileSystemCacheManager(provider, root)
        // ignores the exception
        runCatching { cache.getOrProduce(req) }
        // .tmp must hold no temp dirs left
        val tmpRoot = root.resolve(".tmp")
        val leftovers = Files.list(tmpRoot).use { it.toList() }
        leftovers.size shouldBe 0
    }

    "empty result: produce leaves no file and throws IllegalStateException, nothing promoted" {
        val root = newRoot()
        val req = request("entry_empty")
        val provider = FakeCopernicusProvider { _, _ -> } // writes nothing
        val cache = FileSystemCacheManager(provider, root)
        shouldThrow<IllegalStateException> {
            cache.getOrProduce(req)
        }
        root.resolve(req.toFileName()).shouldNotExist()
    }

    "validate before promoting: a dir with only subdirs (no regular file) is rejected" {
        val root = newRoot()
        val req = request("entry_subdir")
        val provider = FakeCopernicusProvider { _, dir ->
            // a directory, but no regular file
            Files.createDirectory(dir.resolve("nested"))
        }
        val cache = FileSystemCacheManager(provider, root)
        shouldThrow<IllegalStateException> {
            cache.getOrProduce(req)
        }
        root.resolve(req.toFileName()).shouldNotExist()
    }

    "hit is detected even across a fresh CacheManager over the same root" {
        val root = newRoot()
        val req = request("entry_persist")
        val firstProvider = FakeCopernicusProvider { _, dir ->
            Files.writeString(dir.resolve(dataFileName), "first")
        }
        FileSystemCacheManager(firstProvider, root).getOrProduce(req)
        val secondProvider = FakeCopernicusProvider { _, dir ->
            Files.writeString(dir.resolve(dataFileName), "second")
        }
        val result = FileSystemCacheManager(secondProvider, root).getOrProduce(req)
        Files.readString(result.resolve(dataFileName)) shouldBe "first"
    }
})
