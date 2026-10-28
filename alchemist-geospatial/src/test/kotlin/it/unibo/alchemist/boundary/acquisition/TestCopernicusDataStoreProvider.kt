/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import it.unibo.alchemist.loadJsonCopernicusResponse
import java.net.URI
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class TestCopernicusDataStoreProvider {

    private val token = "test-token"
    private val successful = "successful-status"
    private val accepted = "accepted-status"

    private val standardProvider = CopernicusDataStoreProvider { token }

    /**
     * The routes and captured bodies of one data store's job lifecycle.
     *
     * The four bodies captured from a store (submit, accepted, successful, results) all describe the
     * same job, so every route the provider will request during that lifecycle is derivable from
     * [dataset] and [jobId].
     *
     * @property name the store's identifier.
     * @property dataset the dataset requested.
     * @property jobId the identifier of the captured job, shared by all the store's bodies.
     * @property assetPath the path component of the captured download URL, i.e. the route the fake
     * object store must serve.
     */
    private data class Store(
        val name: String,
        val dataset: String,
        val jobId: String,
        val assetPath: String,
    ) {
        val submitRoute: String get() = "/retrieve/v1/processes/$dataset/execution"
        val jobRoute: String get() = "/retrieve/v1/jobs/$jobId"
        val resultsRoute: String get() = "$jobRoute/results"
        val assetName: String get() = assetPath.substringAfterLast('/')

        /**
         * @return the captured body of the given [action] (e.g., "submit")
         */
        fun body(action: String): String = loadBody("$name-$action.json")
    }

    private val cds = Store(
        name = "cds",
        dataset = "derived-era5-land-daily-statistics",
        jobId = "82d0a5fb-f096-42fd-b644-c7ba173b0154",
        assetPath = "/cci2-prod-cache-1/2026-08-08/f8ec201f667455bd3cf338c39fc03a1a.zip",
    )
    private val ads = Store(
        name = "ads",
        dataset = "cams-global-greenhouse-gas-forecasts",
        jobId = "a79e0cce-9c46-4bd9-aec5-3570977cbbd1",
        assetPath = "/cci2-prod-cache-2/2026-08-08/1b2c8f7e437451ffc09a9e23cb32a542.zip",
    )
    private val ewds = Store(
        name = "ewds",
        dataset = "efas-historical",
        jobId = "bb1ee550-0dea-4c84-b164-b8c54165a25f",
        assetPath = "/cci2-prod-cache-3/2026-08-09/9600fbec69609809250b901b42f6800.zip",
    )

    private fun emptyRequest(endpoint: String, dataset: String) = CopernicusRequest(endpoint, dataset, mapOf())

    /**
     * The real bases appearing in the captured bodies.
     */
    private val realBases = setOf(
        "https://cds.climate.copernicus.eu/api",
        "https://ewds.climate.copernicus.eu/api",
        "https://ads.atmosphere.copernicus.eu/api",
        "https://object-store.os-api.cci2.ecmwf.int:443",
    )
    private val realHosts = realBases.mapTo(mutableSetOf()) { URI.create(it).host }

    /**
     * Rewrites the real data-store and object-store bases of a captured body into the loopback
     * address of [fake].
     *
     * Fails if any host that should have been rewritten survives: without this check, a base
     * written differently from the ones listed in [realBases] would not produce a test
     * failure, it would send a REAL network request to ECMWF.
     */
    private fun String.withFakeBase(fake: FakeHttpServer): String {
        // matches the host of every absolute http URL that appears as a JSON `href` value.
        val followedHost = Regex(""""href"\s*:\s*"https?://([^/"]+)""")
        val replaced = realBases.fold(this) { body, base ->
            body.replace(base, fake.baseUrl)
        }
        val items = followedHost.findAll(replaced)
            .map { it.groupValues[1].substringBefore(':') }
            .filterTo(mutableSetOf()) { it in realHosts }
        check(items.isEmpty()) {
            "The body still points at $items after the rewrite: the provider would hit the network"
        }
        return replaced
    }

    /**
     * Rewrites a captured results body so that it advertises the payload the fake object store
     * will actually serve. A null [checksum] reproduces a store that advertises none.
     */
    private fun String.advertising(sizeBytes: Int, checksum: String?): String = this
        .replace(Regex("\"file:size\": *\\d+")) { "\"file:size\": $sizeBytes" }
        .replace(Regex("\"file:checksum\": *\"[^\"]*\"")) {
            checksum?.let { hex -> "\"file:checksum\": \"$hex\"" } ?: "\"file:checksum\": null"
        }

    /**
     * Replays [store]'s captured submit body: the provider then follows its `rel="monitor"` link.
     */
    private fun FakeHttpServer.replaySubmit(store: Store) {
        val body = store.body("submit").withFakeBase(this)
        enqueue("POST", store.submitRoute) { FakeHttpServer.json(201, body)(it) }
    }

    /**
     * Replays one of [store]'s captured status bodies as the next poll response.
     */
    private fun FakeHttpServer.replayStatus(store: Store, action: String) {
        val body = store.body(action).withFakeBase(this)
        enqueue("GET", store.jobRoute) { FakeHttpServer.json(200, body)(it) }
    }

    /**
     * Answers the next poll of [store] with a handwritten [json].
     */
    private fun FakeHttpServer.answerStatus(store: Store, json: String) =
        enqueue("GET", store.jobRoute) { FakeHttpServer.json(200, json)(it) }

    /**
     * Replays [store]'s captured results body, rewritten to describe the payload being served.
     */
    private fun FakeHttpServer.replayResults(store: Store, sizeBytes: Int, checksum: String? = null) {
        val body = store.body("results").withFakeBase(this).advertising(sizeBytes, checksum)
        enqueue("GET", store.resultsRoute) { FakeHttpServer.json(200, body)(it) }
    }

    /**
     * Serves [payload] from the fake object store at [store]'s asset path.
     */
    private fun FakeHttpServer.serveAsset(store: Store, payload: ByteArray) =
        constant("GET", store.assetPath) { FakeHttpServer.bytes(200, payload)(it) }

    // FULL OGC CONVERSATION SIMULATION
    @Test
    fun `completes the OGC workflow on the captured CDS conversation and downloads the asset`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            val payload = "test".toByteArray()
            fake.replaySubmit(cds)
            fake.replayStatus(cds, accepted)
            fake.replayStatus(cds, successful)
            fake.replayResults(cds, payload.size)
            fake.serveAsset(cds, payload)
            val request = CopernicusRequest(fake.baseUrl, cds.dataset, mapOf("day" to "01"))
            standardProvider.load(request, tempDir)
            val downloaded = tempDir.resolve(cds.assetName)
            assertTrue(downloaded.exists(), "Expected $downloaded to exist")
            assertContentEquals(payload, downloaded.readBytes())
            // the download must NOT carry the token
            val downloadReq = fake.requests.single { it.route == cds.assetPath }
            val tokenHeader = "PRIVATE-TOKEN"
            assertNull(downloadReq.header(tokenHeader))
            // every other OGC request MUST carry the token
            fake.requests.filter { it !== downloadReq }.forEach {
                assertEquals(token, it.header(tokenHeader))
            }
        }
    }

    /*
     * The data store emits hexadecimal hashes WITHOUT left zero-padding, so an intact asset whose
     * MD5 begins with a zero is advertised with 31 characters. Payload "a" has MD5
     * 0cc175b9c0f1b6a831c399e269772661 (computed with `md5sum`); the store would
     * announce it stripped of its leading zero, and the download must still be accepted.
     */
    @Test
    fun `accepts an asset whose advertised MD5 was served without its leading zero`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            val payload = "a".toByteArray()
            fake.replaySubmit(cds)
            fake.replayStatus(cds, successful)
            fake.replayResults(cds, payload.size, checksum = "cc175b9c0f1b6a831c399e269772661")
            fake.serveAsset(cds, payload)
            val request = emptyRequest(fake.baseUrl, cds.dataset)
            standardProvider.load(request, tempDir)
            assertContentEquals(payload, tempDir.resolve(cds.assetName).readBytes())
        }
    }

    private fun assertThrowsOnTerminalState(status: String, message: String) {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(ewds)
            fake.answerStatus(
                ewds,
                """
                {
                  "processID": "${ewds.dataset}",
                  "jobID": "${ewds.jobId}",
                  "status": "$status",
                  "message": "$message"
                }
                """.trimIndent(),
            )
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ewds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, status)
            assertMessageContains(ex, message)
        }
    }

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
    fun `reports the backend traceback of a failed job by referencing its results link`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(ewds)
            fake.answerStatus(
                ewds,
                """
                {
                  "processID": "${ewds.dataset}",
                  "jobID": "${ewds.jobId}",
                  "status": "failed",
                  "links": [
                    { "href": "${fake.baseUrl}${ewds.resultsRoute}", "rel": "results" }
                  ]
                }
                """.trimIndent(),
            )
            fake.enqueue("GET", ewds.resultsRoute) {
                FakeHttpServer.json(400, loadBody("ewds-failed-results.json").withFakeBase(fake))(it)
            }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ewds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "failed")
            assertMessageContains(ex, "MultiAdaptorNoDataError")
            assertMessageContains(ex, "4e257fcd-dda1-494a-8e26-d5d6885676d4")
        }
    }

    @Test
    fun `keeps polling on an undocumented status instead of failing`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(cds)
            // an unforeseen status must be treated as transient, not as terminal
            fake.answerStatus(cds, """{ "status": "queued_for_retry" }""")
            fake.replayStatus(cds, successful)
            fake.replayResults(cds, sizeBytes = 0)
            fake.serveAsset(cds, ByteArray(0))
            val request = emptyRequest(fake.baseUrl, cds.dataset)
            standardProvider.load(request, tempDir)
            val downloaded = tempDir.resolve(cds.assetName)
            assertTrue(downloaded.exists(), "Expected $downloaded to exist")
        }
    }

    @Test
    fun `times out if the job never completes`() {
        FakeHttpServer().use { fake ->
            val provider = CopernicusDataStoreProvider(timeout = 500.milliseconds) { token }
            val tempDir = createTempDirectory()
            fake.replaySubmit(cds)
            fake.constant("GET", cds.jobRoute) { FakeHttpServer.json(200, """{ "status": "running" }""")(it) }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, cds.dataset)
                provider.load(request, tempDir)
            }
            assertMessageContains(ex, "Timeout")
        }
    }

    @Test
    fun `reports a 404 result-not-ready returned by the results endpoint`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(ads)
            fake.replayStatus(ads, successful)
            fake.enqueue("GET", ads.resultsRoute) {
                FakeHttpServer.json(404, loadBody("error-404-result-not-ready.json").withFakeBase(fake))(it)
            }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ads.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "404")
            assertMessageContains(ex, "result-not-ready")
        }
    }

    @Test
    fun `reports the problem-detail of a 401 raised while polling`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(ads)
            fake.enqueue("GET", ads.jobRoute) {
                FakeHttpServer.json(401, loadBody("error-401-permission-denied.json").withFakeBase(fake))(it)
            }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ads.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "401")
            assertMessageContains(ex, "authentication required")
        }
    }

    @Test
    fun `reports the problem-detail of a 400 rejection at submit`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            val rejection = loadBody("error-400-rejected.json")
            fake.enqueue("POST", ewds.submitRoute) { FakeHttpServer.json(400, rejection)(it) }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ewds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "400")
            assertMessageContains(ex, "valid combination of values")
            assertMessageContains(ex, "cec329b8-cb55-4b84-a3a8-86b85facdbb4")
        }
    }

    @Test
    fun `falls back to the raw body when a 422 carries an array-valued detail`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            // synthetic response (could not capture one)
            val validation = """
                {
                  "detail": [
                    { "loc": ["body", "inputs", "hyear"], "msg": "field required", "type": "value_error.missing" }
                  ]
                }
            """.trimIndent()
            fake.enqueue("POST", ewds.submitRoute) { FakeHttpServer.json(422, validation)(it) }
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, ewds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "422")
            assertMessageContains(ex, "field required")
        }
    }

    @Test
    fun `fails when a 'successful' job exposes no rel='results' link (inconsistent server response)`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(cds)
            fake.answerStatus(cds, """{ "status": "successful", "links": [] }""")
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, cds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "rel='results'")
        }
    }

    @Test
    fun `fails with a clear message when the downloaded size does not match`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            val payload = "test".toByteArray()
            fake.replaySubmit(cds)
            fake.replayStatus(cds, successful)
            // advertises one byte more than the object store will actually serve
            fake.replayResults(cds, payload.size + 1)
            fake.serveAsset(cds, payload)
            val ex = assertFailsWith<IllegalStateException> {
                val request = emptyRequest(fake.baseUrl, cds.dataset)
                standardProvider.load(request, tempDir)
            }
            assertMessageContains(ex, "Size mismatch")
        }
    }

    @Test
    fun `sends the serialized inputs in the submit body`() {
        FakeHttpServer().use { fake ->
            val tempDir = createTempDirectory()
            fake.replaySubmit(cds)
            fake.replayStatus(cds, successful)
            fake.replayResults(cds, sizeBytes = 0)
            fake.serveAsset(cds, ByteArray(0))
            val request = CopernicusRequest(fake.baseUrl, cds.dataset, mapOf("year" to "2023"))
            standardProvider.load(request, tempDir)
            val submit = fake.requests.single { it.method == "POST" }
            listOf("\"inputs\"", "\"year\"", "2023").forEach { expected ->
                assertTrue(
                    expected in submit.body,
                    "Expected the submit body to contain $expected, but it was: ${submit.body}",
                )
            }
        }
    }
}

private fun assertMessageContains(exception: Throwable, expected: String) {
    val message = exception.message
    assertTrue(
        message != null && expected in message,
        "Expected the exception message to contain '$expected', but it was: $message",
    )
}

private fun loadBody(fileName: String): String = loadJsonCopernicusResponse(
    fileName,
    TestCopernicusDataStoreProvider::class.java,
)
