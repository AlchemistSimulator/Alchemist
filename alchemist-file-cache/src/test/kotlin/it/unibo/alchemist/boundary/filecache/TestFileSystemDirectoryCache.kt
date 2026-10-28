/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

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
 * A [CachedDirectoryKey] whose directory name is its [id].
 */
private data class TestKey(val id: String) : CachedDirectoryKey {
    override val directoryName: String get() = id
}

/**
 * Delegates each load call to a configurable [behavior], so that [FileSystemDirectoryCache] can
 * be tested fully offline.
 */
private class FakeLoader(private val behavior: (key: TestKey, target: Path) -> Unit) : CacheableDataLoader<TestKey> {

    var calls: Int = 0
        private set

    override fun load(key: TestKey, target: Path) {
        calls++
        behavior(key, target)
    }
}

class TestFileSystemDirectoryCache : StringSpec({

    val tempDir: Path = Files.createTempDirectory("directory-cache-test")

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
     * A minimal [CachedDirectoryKey] for a test case.
     */
    fun testKey(id: String): TestKey = TestKey(id)

    "miss: the loader runs exactly once and its files are promoted" {
        val key = testKey("entry_a")
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val cache = FileSystemDirectoryCache(loader, newRoot())
        val result = cache[key]
        loader.calls shouldBe 1
        result.shouldExist()
        result.resolve(dataFileName).shouldExist()
    }

    "hit: the second call reuses the first entry without reloading" {
        val key = testKey("entry_b")
        var marker = "first"
        val loader = FakeLoader { _, dir -> Files.writeString(dir.resolve(dataFileName), marker) }
        val cache = FileSystemDirectoryCache(loader, newRoot())
        val first = cache[key]
        // if the cache wrongly re-ran the loader, the file would contain "second"
        marker = "second"
        val second = cache[key]
        second shouldBe first
        Files.readString(second.resolve(dataFileName)) shouldBe "first"
        loader.calls shouldBe 1
    }

    "the returned directory name is exactly key.directoryName under root" {
        val root = newRoot()
        val key = testKey("dataset_abc123")
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val cache = FileSystemDirectoryCache(loader, root)
        val result = cache[key]
        result shouldBe root.resolve(key.directoryName)
    }

    "load failure: the exception propagates and no entry is promoted" {
        val root = newRoot()
        val key = testKey("entry_fail")
        val loader = FakeLoader { _, _ -> error("load blew up") }
        val cache = FileSystemDirectoryCache(loader, root)
        shouldThrow<IllegalStateException> { cache[key] }
        root.resolve(key.directoryName).shouldNotExist() // no poisoned dir
    }

    "load failure: the temporary directory is cleaned up, leaving .tmp empty" {
        val root = newRoot()
        val key = testKey("entry_fail2")
        val loader = FakeLoader { _, dir ->
            Files.writeString(dir.resolve("partial.nc"), "half")
            // simulates something gone wrong after having already written a file
            error("error after writing")
        }
        val cache = FileSystemDirectoryCache(loader, root)
        // ignores the exception
        runCatching { cache[key] }
        // .tmp must hold no temp dirs left
        val tmpRoot = root.resolve(".tmp")
        val leftovers = Files.list(tmpRoot).use { it.toList() }
        leftovers.size shouldBe 0
    }

    "empty result: the loader leaves no file and throws IllegalStateException, nothing promoted" {
        val root = newRoot()
        val key = testKey("entry_empty")
        val loader = FakeLoader { _, _ -> } // writes nothing
        val cache = FileSystemDirectoryCache(loader, root)
        shouldThrow<IllegalStateException> {
            cache[key]
        }
        root.resolve(key.directoryName).shouldNotExist()
    }

    "validate before promoting: a dir with only subdirs (no regular file) is rejected" {
        val root = newRoot()
        val key = testKey("entry_subdir")
        val loader = FakeLoader { _, dir ->
            // a directory, but no regular file
            Files.createDirectory(dir.resolve("nested"))
        }
        val cache = FileSystemDirectoryCache(loader, root)
        shouldThrow<IllegalStateException> {
            cache[key]
        }
        root.resolve(key.directoryName).shouldNotExist()
    }

    "hit is detected even across a fresh cache over the same root" {
        val root = newRoot()
        val key = testKey("entry_persist")
        val firstLoader = FakeLoader { _, dir ->
            Files.writeString(dir.resolve(dataFileName), "first")
        }
        FileSystemDirectoryCache(firstLoader, root)[key]
        val secondLoader = FakeLoader { _, dir ->
            Files.writeString(dir.resolve(dataFileName), "second")
        }
        val result = FileSystemDirectoryCache(secondLoader, root)[key]
        Files.readString(result.resolve(dataFileName)) shouldBe "first"
    }

    // multi thread tests
    "concurrent misses on the same cache entry are loaded exactly once" {
        val root = newRoot()
        val key = testKey("entry_concurrent")
        val threadCount = 3
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val cache = FileSystemDirectoryCache(loader, root)
        val results = ConcurrentLinkedQueue<Path>()
        concurrently(
            List(threadCount) {
                { results.add(cache[key]) }
            },
        )
        loader.calls shouldBe 1
        results.size shouldBe threadCount
        results.toSet() shouldBe setOf(root.resolve(key.directoryName))
    }

    "two caches over the same root load a shared entry only once" {
        val root = newRoot()
        val key = testKey("entry_two_caches")
        val firstLoader = FakeLoader { _, dir -> writeOneFile(dir) }
        val secondLoader = FakeLoader { _, dir -> writeOneFile(dir) }
        val firstCache = FileSystemDirectoryCache(firstLoader, root)
        val secondCache = FileSystemDirectoryCache(secondLoader, root)
        concurrently(
            listOf(
                { firstCache[key] },
                { secondCache[key] },
            ),
        )
        firstLoader.calls + secondLoader.calls shouldBe 1
    }

    "a cache entry published by another process during load is kept and the local copy gets discarded" {
        val root = newRoot()
        val key = testKey("race_lost_test")
        val finalDir = root.resolve(key.directoryName)
        val winnerStr = "I won!"
        val loader = FakeLoader { _, dir ->
            // the local work that should lose the race
            Files.writeString(dir.resolve(dataFileName), "local")
            // simulates another process that publishes the exact same cache entry
            Files.createDirectories(finalDir)
            Files.writeString(finalDir.resolve(dataFileName), winnerStr)
        }
        val result = FileSystemDirectoryCache(loader, root)[key]
        loader.calls shouldBe 1
        result shouldBe finalDir
        Files.readString(result.resolve(dataFileName)) shouldBe winnerStr
        // the local copy should get discarded
        Files.list(root.resolve(".tmp")).use { it.toList() }.shouldBeEmpty()
    }
})
