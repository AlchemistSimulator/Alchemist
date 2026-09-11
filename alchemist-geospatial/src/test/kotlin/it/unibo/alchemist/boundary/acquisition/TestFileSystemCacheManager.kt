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
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch

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

    // runs every task on its own thread, releases them all at once, and rethrows any failure
    fun concurrently(tasks: List<() -> Unit>) {
        val gate = CountDownLatch(1)
        // threads can safely put the exceptions here
        val failures = ConcurrentLinkedQueue<Throwable>()
        // creates a thread for each task
        val threads = tasks.map { task ->
            Thread {
                // blocks execution until gate reaches 0
                gate.await()
                runCatching { task() }.onFailure { failures.add(it) }
            }.apply {
                // daemon flag enabled, so the JVM can terminate
                isDaemon = true
                start()
            }
        }
        // simultaneously starts all the set-up threads
        gate.countDown()
        // maximum number of milliseconds to wait for a thread to finish. Used to prevent deadlocks
        threads.forEach { it.join(10_000L) }
        threads.forEach { it.isAlive shouldBe false }
        failures.toList().shouldBeEmpty()
    }

    /**
     * A minimal [CopernicusRequest] for a test case.
     * The inputs remain constant, only the dataset changes.
     */
    fun request(id: String): CopernicusRequest = CopernicusRequest(
        endpoint = "https://ewds.climate.copernicus.eu/api",
        dataset = id,
        inputs = emptyMap(),
    )

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

    "the returned directory name is exactly request.toDirectoryName() under root" {
        val root = newRoot()
        val req = request("cems-glofas_abc123")
        val provider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val cache = FileSystemCacheManager(provider, root)
        val result = cache.getOrProduce(req)
        result shouldBe root.resolve(req.toDirectoryName())
    }

    "produce failure: the exception propagates and no entry is promoted" {
        val root = newRoot()
        val req = request("entry_fail")
        val provider = FakeCopernicusProvider { _, _ -> error("download blew up") }
        val cache = FileSystemCacheManager(provider, root)
        shouldThrow<IllegalStateException> { cache.getOrProduce(req) }
        root.resolve(req.toDirectoryName()).shouldNotExist() // no poisoned dir
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
        root.resolve(req.toDirectoryName()).shouldNotExist()
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
        root.resolve(req.toDirectoryName()).shouldNotExist()
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

    // multi thread tests
    "concurrent misses on the same cache entry are fetched exactly once" {
        val root = newRoot()
        val req = request("entry_concurrent")
        val threadCount = 3
        val provider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val cache = FileSystemCacheManager(provider, root)
        val results = ConcurrentLinkedQueue<Path>()
        concurrently(
            List(threadCount) {
                { results.add(cache.getOrProduce(req)) }
            },
        )
        provider.calls shouldBe 1
        results.size shouldBe threadCount
        results.toSet() shouldBe setOf(root.resolve(req.toDirectoryName()))
    }

    "two managers over the same root fetch a shared entry only once" {
        val root = newRoot()
        val req = request("entry_two_managers")
        val firstProvider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val secondProvider = FakeCopernicusProvider { _, dir -> writeOneFile(dir) }
        val firstCache = FileSystemCacheManager(firstProvider, root)
        val secondCache = FileSystemCacheManager(secondProvider, root)
        concurrently(
            listOf(
                { firstCache.getOrProduce(req) },
                { secondCache.getOrProduce(req) },
            ),
        )
        firstProvider.calls + secondProvider.calls shouldBe 1
    }

    "a cache entry published by another process during fetch is kept and the local copy gets discarded" {
        val root = newRoot()
        val req = request("race_lost_test")
        val finalDir = root.resolve(req.toDirectoryName())
        val winnerStr = "I won!"
        val provider = FakeCopernicusProvider { _, dir ->
            // the local work that should lose the race
            Files.writeString(dir.resolve(dataFileName), "local")
            // simulates another process that publishes the exact same cache entry
            Files.createDirectories(finalDir)
            Files.writeString(finalDir.resolve(dataFileName), winnerStr)
        }
        val result = FileSystemCacheManager(provider, root).getOrProduce(req)
        provider.calls shouldBe 1
        result shouldBe finalDir
        Files.readString(result.resolve(dataFileName)) shouldBe winnerStr
        // the local copy should get discarded
        Files.list(root.resolve(".tmp")).use { it.toList() }.shouldBeEmpty()
    }
})
