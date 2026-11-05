/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import it.unibo.alchemist.boundary.copernicus.FakeDataStore.CapturedStore
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.ACCEPTED
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.SUCCESSFUL
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.TOKEN
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.TOKEN_HEADER
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.capturedBody
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.fastProvider
import it.unibo.alchemist.boundary.copernicus.FakeDataStore.Companion.zipOf
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.milliseconds

class TestCopernicusDataStoreProvider {

    private val payload = "test".toByteArray()

    // successful workflows
    @Test
    fun `completes the OGC workflow on the captured CDS conversation and downloads the asset`() {
        FakeDataStore().use { fake ->
            fake.replaySubmit()
            fake.replayStatus(ACCEPTED)
            fake.succeedingWith(payload)
            val target = fake.download(mapOf("day" to "01"))
            // the payload is not a ZIP archive, so it is kept as it is, whatever its name
            assertContentEquals(payload, target.resolve(fake.store.assetName).readBytes())
            // the download must NOT carry the token, while every other request must
            val (download, others) = fake.server.requests.partition { it.route == fake.store.assetPath }
            assertNull(download.single().header(TOKEN_HEADER))
            others.forEach { assertEquals(TOKEN, it.header(TOKEN_HEADER)) }
        }
    }

    @Test
    fun `sends the serialized inputs in the submit body`() {
        FakeDataStore().use { fake ->
            fake.completingWith(payload).download(mapOf("year" to "2023"))
            val submit = fake.server.requests.single { it.method == "POST" }
            listOf("\"inputs\"", "\"year\"", "2023").forEach { assertContains(submit.body, it) }
        }
    }

    @Test
    fun `keeps polling on an undocumented status instead of failing`() {
        FakeDataStore().use { fake ->
            fake.replaySubmit()
            // an unforeseen status must be treated as transient, not as terminal
            fake.answerStatus("""{ "status": "queued_for_retry" }""")
            fake.succeedingWith(payload)
            assertEquals(setOf(fake.store.assetName), fake.download().fileNames())
        }
    }

    // integrity checks
    @Test
    fun `accepts an asset advertised with no MD5`() {
        FakeDataStore().use { fake ->
            val target = fake.completingWith(payload, checksum = null).download()
            assertEquals(setOf(fake.store.assetName), target.fileNames())
        }
    }

    @Test
    fun `accepts an asset advertised with an uppercase MD5`() {
        FakeDataStore().use { fake ->
            // md5("test")
            fake.completingWith(payload, checksum = "098F6BCD4621D373CADE4E832627B4F6").download()
        }
    }

    /*
     * The data store emits hexadecimal hashes WITHOUT left zero-padding, so an intact asset whose
     * MD5 begins with a zero is advertised with 31 characters. Payload "a" has MD5
     * 0cc175b9c0f1b6a831c399e269772661; the store announces it stripped of its leading zero.
     */
    @Test
    fun `accepts an asset whose advertised MD5 was served without its leading zero`() {
        FakeDataStore().use { fake ->
            val payload = "a".toByteArray()
            val target = fake.completingWith(payload, checksum = "cc175b9c0f1b6a831c399e269772661").download()
            assertContentEquals(payload, target.resolve(fake.store.assetName).readBytes())
        }
    }

    @Test
    fun `fails with a clear message when the downloaded size does not match`() {
        FakeDataStore().use { fake ->
            // advertises one byte more than the object store actually serves
            val failure = fake.completingWith(payload, sizeBytes = payload.size + 1).failingDownload()
            assertMessageContains(failure, "Size mismatch")
        }
    }

    @Test
    fun `fails with a clear message when the MD5 does not match`() {
        FakeDataStore().use { fake ->
            val failure = fake.completingWith(payload, checksum = "a".repeat(32)).failingDownload()
            assertMessageContains(failure, "MD5 mismatch")
        }
    }

    @Test
    fun `fails with a clear message when the advertised MD5 is unusable`() {
        FakeDataStore().use { fake ->
            val failure = fake.completingWith(payload, checksum = "not-an-md5").failingDownload()
            assertMessageContains(failure, "unusable MD5")
        }
    }

    // archives
    @Test
    fun `extracts the downloaded archives, flattening their entries, and deletes them`() {
        FakeDataStore().use { fake ->
            val archive = zipOf("a.nc" to "a".toByteArray(), "nested/directory/b.nc" to "b".toByteArray())
            val target = fake.completingWith(archive).download()
            assertEquals(setOf("a.nc", "b.nc"), target.fileNames())
            assertContentEquals("b".toByteArray(), target.resolve("b.nc").readBytes())
        }
    }

    @Test
    fun `fails when the entries of an archive share a name`() {
        FakeDataStore().use { fake ->
            val archive = zipOf("first/a.nc" to "1".toByteArray(), "second/a.nc" to "2".toByteArray())
            assertMessageContains(fake.completingWith(archive).failingDownload(), "collision", "a.nc")
        }
    }

    @Test
    fun `fails on a corrupt archive instead of keeping it`() {
        FakeDataStore().use { fake ->
            // without its end of central directory record, the archive cannot be read, but still looks like a ZIP
            val archive = zipOf("a.nc" to "a".toByteArray()).let { it.copyOf(it.size - END_OF_CENTRAL_DIRECTORY_BYTES) }
            assertFailsWith<IOException> { fake.completingWith(archive).download() }
        }
    }

    // failed jobs
    @Test
    fun `throws on the terminal state 'failed', reporting the job message`() =
        assertThrowsOnTerminalState("failed", "Internal processing error: out of memory")

    @Test
    fun `throws on the terminal state 'rejected', reporting the job message`() =
        assertThrowsOnTerminalState("rejected", "Job rejected: malformed processing chain")

    @Test
    fun `throws on the terminal state 'dismissed', reporting the job message`() =
        assertThrowsOnTerminalState("dismissed", "Job dismissed by the user")

    @Test
    fun `reports the whole status of a failed job with neither results nor message`() {
        FakeDataStore(CapturedStore.EWDS).use { fake ->
            fake.replaySubmit()
            fake.answerStatus(fake.failedJob("failed"))
            assertMessageContains(fake.failingDownload(), "failed", "Body:", fake.store.jobId)
        }
    }

    @Test
    fun `reports the backend traceback of a failed job by referencing its results link`() {
        FakeDataStore(CapturedStore.EWDS).use { fake ->
            fake.replaySubmit()
            val results = """[ { "href": "${fake.endpoint}${fake.store.resultsRoute}", "rel": "results" } ]"""
            fake.answerStatus(fake.failedJob("failed", links = results))
            fake.answerResults(400, capturedBody("ewds-failed-results.json"))
            assertMessageContains(
                fake.failingDownload(),
                "failed",
                "MultiAdaptorNoDataError",
                "4e257fcd-dda1-494a-8e26-d5d6885676d4",
            )
        }
    }

    @Test
    fun `times out if the job never completes`() {
        FakeDataStore().use { fake ->
            fake.replaySubmit()
            fake.answerStatusForever("""{ "status": "running" }""")
            assertMessageContains(fake.failingDownload(provider = fastProvider(timeout = 500.milliseconds)), "Timeout")
        }
    }

    @Test
    fun `fails when a 'successful' job exposes no rel='results' link (inconsistent server response)`() {
        FakeDataStore().use { fake ->
            fake.replaySubmit()
            fake.answerStatus("""{ "status": "successful", "links": [] }""")
            assertMessageContains(fake.failingDownload(), "rel='results'")
        }
    }

    // HTTP errors
    @Test
    fun `reports a 404 result-not-ready returned by the results endpoint`() {
        FakeDataStore(CapturedStore.ADS).use { fake ->
            fake.replaySubmit()
            fake.replayStatus(SUCCESSFUL)
            fake.answerResults(404, capturedBody("error-404-result-not-ready.json"))
            assertMessageContains(fake.failingDownload(), "404", "result-not-ready")
        }
    }

    @Test
    fun `reports the problem-detail of a 401 raised while polling`() {
        FakeDataStore(CapturedStore.ADS).use { fake ->
            fake.replaySubmit()
            fake.answerPollError(401, capturedBody("error-401-permission-denied.json"))
            assertMessageContains(fake.failingDownload(), "401", "authentication required")
        }
    }

    @Test
    fun `reports the problem-detail of a 400 rejection at submit`() {
        FakeDataStore(CapturedStore.EWDS).use { fake ->
            fake.answerSubmit(400, capturedBody("error-400-rejected.json"))
            assertMessageContains(
                fake.failingDownload(),
                "400",
                "valid combination of values",
                "cec329b8-cb55-4b84-a3a8-86b85facdbb4",
            )
        }
    }

    @Test
    fun `falls back to the raw body when a 422 carries an array-valued detail`() {
        FakeDataStore(CapturedStore.EWDS).use { fake ->
            // synthetic response (could not capture one)
            fake.answerSubmit(
                422,
                """
                {
                  "detail": [
                    { "loc": ["body", "inputs", "hyear"], "msg": "field required", "type": "value_error.missing" }
                  ]
                }
                """.trimIndent(),
            )
            assertMessageContains(fake.failingDownload(), "422", "field required")
        }
    }

    private fun assertMessageContains(exception: Throwable, vararg expected: String) =
        expected.forEach { assertContains(exception.message.orEmpty(), it) }

    private fun assertThrowsOnTerminalState(status: String, message: String) {
        FakeDataStore(CapturedStore.EWDS).use { fake ->
            fake.replaySubmit()
            fake.answerStatus(fake.failedJob(status, message))
            assertMessageContains(fake.failingDownload(), status, message)
        }
    }

    /**
     * A failed job with the given [status], [message], and links, as reported by a poll.
     */
    private fun FakeDataStore.failedJob(status: String, message: String? = null, links: String = "[]") = """
        {
          "processID": "${store.dataset}",
          "jobID": "${store.jobId}",
          "status": "$status",
          ${message?.let { "\"message\": \"$it\"," }.orEmpty()}
          "links": $links
        }
    """.trimIndent()

    private fun Path.fileNames(): Set<String> = listDirectoryEntries().mapTo(mutableSetOf()) { it.name }

    private companion object {
        // the size of the end of central directory record of a ZIP archive with no comment
        const val END_OF_CENTRAL_DIRECTORY_BYTES = 22
    }
}
