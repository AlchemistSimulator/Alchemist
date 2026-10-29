/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.filecache

import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.io.path.exists
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

class TestFileSystemDirectoryCache {

    private val tempDir: Path = Files.createTempDirectory("directory-cache-test")

    private val root: Path = tempDir.resolve("root")

    @AfterTest
    fun deleteTempDir() {
        tempDir.toFile().deleteRecursively()
    }

    private fun writeOneFile(dir: Path) {
        Files.writeString(dir.resolve(DATA_FILE), "payload")
    }

    private fun Path.content(): String = Files.readString(resolve(DATA_FILE))

    /**
     * Runs every task on its own thread, releases them all at once, waits for all of them,
     * and rethrows the first failure, if any.
     */
    private fun concurrently(tasks: List<() -> Unit>) {
        val gate = CountDownLatch(1)
        val failures = ConcurrentLinkedQueue<Throwable>()
        val threads = tasks.map { task ->
            Thread {
                gate.await()
                runCatching { task() }.onFailure { failures.add(it) }
            }.apply { start() }
        }
        gate.countDown()
        threads.forEach(Thread::join)
        failures.firstOrNull()?.let { throw it }
    }

    @Test
    fun `on a miss, the loader runs exactly once and its files are promoted`() {
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val result = FileSystemDirectoryCache(loader, root)[TestKey("entry_a")]
        assertEquals(1, loader.calls)
        assertTrue(result.resolve(DATA_FILE).exists())
    }

    @Test
    fun `on a hit, the entry is reused without loading it again`() {
        var marker = "first"
        val loader = FakeLoader { _, dir -> Files.writeString(dir.resolve(DATA_FILE), marker) }
        val cache = FileSystemDirectoryCache(loader, root)
        val first = cache[TestKey("entry_b")]
        // if the cache wrongly re-ran the loader, the file would contain "second"
        marker = "second"
        val second = cache[TestKey("entry_b")]
        assertEquals(first, second)
        assertEquals("first", second.content())
        assertEquals(1, loader.calls)
    }

    @Test
    fun `the returned directory is named after the key under the root`() {
        val key = TestKey("dataset_abc123")
        val result = FileSystemDirectoryCache(FakeLoader { _, dir -> writeOneFile(dir) }, root)[key]
        assertEquals(root.resolve(key.directoryName), result)
    }

    @Test
    fun `names that are not a single path segment, or start with a dot, are rejected without loading`() {
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val cache = FileSystemDirectoryCache(loader, root)
        // creates the internal directories, so that their names would otherwise be hits
        cache[TestKey("entry_valid")]
        listOf("", ".", "..", "../outside", "nested/entry", tempDir.resolve("absolute").toString(), ".tmp", ".locks")
            .forEach { name -> assertFailsWith<IllegalArgumentException>("'$name'") { cache[TestKey(name)] } }
        assertEquals(1, loader.calls)
        assertFalse(tempDir.resolve("outside").exists())
    }

    @Test
    fun `a failure of the loader propagates and no entry is promoted`() {
        val key = TestKey("entry_fail")
        val cache = FileSystemDirectoryCache(FakeLoader { _, _ -> error("load blew up") }, root)
        assertFailsWith<IllegalStateException> { cache[key] }
        assertFalse(root.resolve(key.directoryName).exists())
    }

    @Test
    fun `a failure of the loader removes its temporary directory`() {
        val loader = FakeLoader { _, dir ->
            Files.writeString(dir.resolve("partial.nc"), "half")
            // simulates something gone wrong after having already written a file
            error("error after writing")
        }
        assertFailsWith<IllegalStateException> { FileSystemDirectoryCache(loader, root)[TestKey("entry_fail2")] }
        assertEquals(emptyList(), Files.list(root.resolve(".tmp")).use { it.toList() })
    }

    @Test
    fun `a loader writing no file fails and nothing is promoted`() {
        val key = TestKey("entry_empty")
        assertFailsWith<IllegalStateException> { FileSystemDirectoryCache(FakeLoader { _, _ -> }, root)[key] }
        assertFalse(root.resolve(key.directoryName).exists())
    }

    @Test
    fun `a loader writing only directories, and no regular file, fails and nothing is promoted`() {
        val key = TestKey("entry_subdir")
        val loader = FakeLoader { _, dir -> Files.createDirectory(dir.resolve("nested")) }
        assertFailsWith<IllegalStateException> { FileSystemDirectoryCache(loader, root)[key] }
        assertFalse(root.resolve(key.directoryName).exists())
    }

    @Test
    fun `a new cache over the same root finds the entries loaded by a previous one`() {
        val key = TestKey("entry_persist")
        FileSystemDirectoryCache(FakeLoader { _, dir -> Files.writeString(dir.resolve(DATA_FILE), "first") }, root)[key]
        val secondLoader = FakeLoader { _, dir -> Files.writeString(dir.resolve(DATA_FILE), "second") }
        assertEquals("first", FileSystemDirectoryCache(secondLoader, root)[key].content())
        assertEquals(0, secondLoader.calls)
    }

    @Test
    fun `concurrent misses on the same entry load it exactly once`() {
        val key = TestKey("entry_concurrent")
        val threadCount = 3
        val loader = FakeLoader { _, dir -> writeOneFile(dir) }
        val cache = FileSystemDirectoryCache(loader, root)
        val results = ConcurrentLinkedQueue<Path>()
        concurrently(List(threadCount) { { results.add(cache[key]) } })
        assertEquals(1, loader.calls)
        assertEquals(threadCount, results.size)
        assertEquals(setOf(root.resolve(key.directoryName)), results.toSet())
    }

    @Test
    fun `two caches over the same root load a shared entry only once`() {
        val key = TestKey("entry_two_caches")
        val firstLoader = FakeLoader { _, dir -> writeOneFile(dir) }
        val secondLoader = FakeLoader { _, dir -> writeOneFile(dir) }
        val firstCache = FileSystemDirectoryCache(firstLoader, root)
        val secondCache = FileSystemDirectoryCache(secondLoader, root)
        concurrently(listOf({ firstCache[key] }, { secondCache[key] }))
        assertEquals(1, firstLoader.calls + secondLoader.calls)
    }

    @Test
    fun `an entry published by another process during the load is kept, and the local copy is discarded`() {
        val key = TestKey("race_lost_test")
        val finalDir = root.resolve(key.directoryName)
        val loader = FakeLoader { _, dir ->
            // the local work that should lose the race
            Files.writeString(dir.resolve(DATA_FILE), "local")
            // simulates another process publishing the same entry without coordination
            Files.createDirectories(finalDir)
            Files.writeString(finalDir.resolve(DATA_FILE), "I won!")
        }
        val result = FileSystemDirectoryCache(loader, root)[key]
        assertEquals(1, loader.calls)
        assertEquals(finalDir, result)
        assertEquals("I won!", result.content())
        assertEquals(emptyList(), Files.list(root.resolve(".tmp")).use { it.toList() })
    }

    @Test
    fun `a process waits for another process loading the same entry, then reuses it`() {
        val key = TestKey("entry_other_process")
        val loading = tempDir.resolve("loading")
        val release = tempDir.resolve("release")
        val java = ProcessHandle.current().info().command().orElseThrow()
        val otherProcess = ProcessBuilder(
            java,
            "-cp",
            System.getProperty("java.class.path"),
            ENTRY_LOADING_PROCESS,
            root.toString(),
            key.directoryName,
            loading.toString(),
            release.toString(),
        ).inheritIO().start()
        try {
            // the other process is loading the entry, holding its file lock
            awaitWhile { otherProcess.isAlive && !loading.exists() }
            assertTrue(loading.exists(), "The other process terminated before loading the entry")
            val loader = FakeLoader { _, dir -> writeOneFile(dir) }
            val result = AtomicReference<Result<Path>>()
            val waiting = Thread { result.set(runCatching { FileSystemDirectoryCache(loader, root)[key] }) }
            waiting.start()
            // the other process completes the load only once this one waits for the lock, or completed without it
            awaitWhile { waiting.isAlive && !waiting.isWaitingForFileLock() }
            Files.createFile(release)
            waiting.join()
            assertEquals(0, otherProcess.waitFor())
            assertEquals(0, loader.calls)
            assertEquals(OTHER_PROCESS_DATA, result.get().getOrThrow().content())
        } finally {
            otherProcess.destroyForcibly()
        }
    }

    private companion object {
        const val ENTRY_LOADING_PROCESS = "it.unibo.alchemist.boundary.filecache.EntryLoadingProcessKt"

        fun Thread.isWaitingForFileLock(): Boolean =
            stackTrace.any { it.className == "sun.nio.ch.FileChannelImpl" && it.methodName == "lock" }
    }
}
