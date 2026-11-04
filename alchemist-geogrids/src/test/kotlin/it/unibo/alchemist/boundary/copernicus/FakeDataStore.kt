/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import it.unibo.alchemist.loadJsonCopernicusResponse
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.createTempDirectory
import kotlin.test.assertFailsWith
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import okio.ByteString.Companion.toByteString

/**
 * A Copernicus data store on the loopback address, replaying the conversations captured from the real [store].
 * Every route the provider requests during a job is derived from the captured job, so tests only choose
 * the answers, e.g., with [completingWith].
 */
internal class FakeDataStore(val store: CapturedStore = CapturedStore.CDS) : AutoCloseable {

    /**
     * The underlying server, which records the [requests][FakeHttpServer.requests] it receives.
     */
    val server = FakeHttpServer()

    /**
     * Where the [download]s go, deleted on [close].
     */
    private val downloads: Path = createTempDirectory("fake-data-store")

    /**
     * The endpoint to request data to.
     */
    val endpoint: String get() = server.baseUrl

    /**
     * Answers the next poll with the error report [json], sent with the HTTP [code].
     */
    fun answerPollError(code: Int, json: String) = server.enqueue("GET", store.jobRoute, answer(code, json))

    /**
     * Answers the next request of the results with [json], sent with the HTTP [code].
     */
    fun answerResults(code: Int, json: String) = server.enqueue("GET", store.resultsRoute, answer(code, json))

    /**
     * Answers the next poll with [json].
     */
    fun answerStatus(json: String) = server.enqueue("GET", store.jobRoute, answer(200, json))

    /**
     * Answers every poll, once the enqueued answers are over, with [json].
     */
    fun answerStatusForever(json: String) = server.constant("GET", store.jobRoute, answer(200, json))

    /**
     * Answers the next submission with [json], sent with the HTTP [code].
     */
    fun answerSubmit(code: Int, json: String) = server.enqueue("POST", store.submitRoute, answer(code, json))

    /**
     * Accepts the submission of a job that succeeds at the first poll (see [succeedingWith]).
     */
    fun completingWith(
        payload: ByteArray,
        sizeBytes: Int = payload.size,
        checksum: String? = payload.toByteString().md5().hex(),
    ): FakeDataStore = apply {
        replaySubmit()
        succeedingWith(payload, sizeBytes, checksum)
    }

    /**
     * Downloads, with [provider], the [inputs] of the dataset of the [store] into a new directory, and returns it.
     * The directory is deleted on [close].
     */
    fun download(inputs: Map<String, Any> = emptyMap(), provider: CopernicusDataStoreProvider = fastProvider()): Path =
        createTempDirectory(downloads, "download").also { provider.load(request(inputs), it) }

    /**
     * The failure of a [download] of [inputs] with [provider].
     */
    fun failingDownload(
        inputs: Map<String, Any> = emptyMap(),
        provider: CopernicusDataStoreProvider = fastProvider(),
    ): IllegalStateException = assertFailsWith<IllegalStateException> { download(inputs, provider) }

    /**
     * Replays the captured status of the job in [state] (e.g., [ACCEPTED]) as the next poll answer.
     */
    fun replayStatus(state: String) = answerStatus(store.body("$state-status"))

    /**
     * Replays the captured submission, after which the provider follows its `rel="monitor"` link.
     */
    fun replaySubmit() = answerSubmit(201, store.body("submit"))

    /**
     * The request of [inputs] to this data store.
     */
    fun request(inputs: Map<String, Any> = emptyMap()) = CopernicusRequest(endpoint, store.dataset, inputs)

    /**
     * Serves [payload] as the asset of the job.
     */
    fun serveAsset(payload: ByteArray) = server.constant("GET", store.assetPath, FakeHttpServer.bytes(200, payload))

    /**
     * Answers the next poll with the success of the job, whose results advertise [sizeBytes] and [checksum]
     * (none when `null`) and serve [payload].
     */
    fun succeedingWith(
        payload: ByteArray,
        sizeBytes: Int = payload.size,
        checksum: String? = payload.toByteString().md5().hex(),
    ): FakeDataStore = apply {
        replayStatus(SUCCESSFUL)
        replayResults(sizeBytes, checksum)
        serveAsset(payload)
    }

    override fun close() {
        server.close()
        downloads.toFile().deleteRecursively()
    }

    /**
     * An answer with [json], pointing to this fake data store instead of the real ones, sent with the HTTP [code].
     */
    private fun answer(code: Int, json: String) = FakeHttpServer.json(code, json.withFakeBase())

    /**
     * Replays the captured results, advertising [sizeBytes] and [checksum] (none when `null`).
     */
    private fun replayResults(sizeBytes: Int, checksum: String?) {
        val body = store.body("results")
            .replace(Regex("\"file:size\": *\\d+")) { "\"file:size\": $sizeBytes" }
            .replace(Regex("\"file:checksum\": *\"[^\"]*\"")) { _ ->
                "\"file:checksum\": ${checksum?.let { "\"$it\"" } ?: "null"}"
            }
        answerResults(200, body)
    }

    /**
     * This captured body, with the real data and object stores replaced by this fake one.
     * Fails if a real host survives the replacement, as following it would hit the network.
     */
    private fun String.withFakeBase(): String {
        val replaced = realBases.fold(this) { body, base -> body.replace(base, endpoint) }
        val surviving = Regex(""""href"\s*:\s*"https?://([^/":]+)""").findAll(replaced)
            .map { it.groupValues[1] }
            .filterTo(mutableSetOf()) { it in realHosts }
        check(surviving.isEmpty()) { "The body still points at $surviving: the provider would hit the network" }
        return replaced
    }

    /**
     * A job captured from a real data store, whose captured answers are named after it, e.g., `cds-submit.json`.
     *
     * @property dataset the dataset requested.
     * @property jobId the identifier of the job.
     * @property assetPath the path of the asset of the job on the object store.
     */
    enum class CapturedStore(val dataset: String, val jobId: String, val assetPath: String) {
        ADS(
            "cams-global-greenhouse-gas-forecasts",
            "a79e0cce-9c46-4bd9-aec5-3570977cbbd1",
            "/cci2-prod-cache-2/2026-08-08/1b2c8f7e437451ffc09a9e23cb32a542.zip",
        ),
        CDS(
            "derived-era5-land-daily-statistics",
            "82d0a5fb-f096-42fd-b644-c7ba173b0154",
            "/cci2-prod-cache-1/2026-08-08/f8ec201f667455bd3cf338c39fc03a1a.zip",
        ),
        EWDS(
            "efas-historical",
            "bb1ee550-0dea-4c84-b164-b8c54165a25f",
            "/cci2-prod-cache-3/2026-08-09/9600fbec69609809250b901b42f6800.zip",
        ),
        ;

        /**
         * The name of the asset, as downloaded.
         */
        val assetName: String get() = assetPath.substringAfterLast('/')

        /**
         * The route polled to monitor the job.
         */
        val jobRoute: String get() = "/retrieve/v1/jobs/$jobId"

        /**
         * The route of the results of the job.
         */
        val resultsRoute: String get() = "$jobRoute/results"

        /**
         * The route the job is submitted to.
         */
        val submitRoute: String get() = "/retrieve/v1/processes/$dataset/execution"

        /**
         * The captured answer to [action] (e.g., `submit`).
         */
        fun body(action: String): String = capturedBody("${name.lowercase()}-$action.json")
    }

    companion object {
        /**
         * The token sent by the [fastProvider]s.
         */
        const val TOKEN = "test-token"

        /**
         * The status of a job waiting to run.
         */
        const val ACCEPTED = "accepted"

        /**
         * The status of a completed job.
         */
        const val SUCCESSFUL = "successful"

        /**
         * The header carrying the token.
         */
        const val TOKEN_HEADER = "PRIVATE-TOKEN"

        /**
         * The real bases appearing in the captured answers.
         */
        private val realBases = setOf(
            "https://cds.climate.copernicus.eu/api",
            "https://ewds.climate.copernicus.eu/api",
            "https://ads.atmosphere.copernicus.eu/api",
            "https://object-store.os-api.cci2.ecmwf.int:443",
        )

        private val realHosts = realBases.mapTo(mutableSetOf()) { URI.create(it).host }

        /**
         * The captured answer in [fileName], among the `copernicus-responses` resources.
         */
        fun capturedBody(fileName: String): String = loadJsonCopernicusResponse(fileName, FakeDataStore::class.java)

        /**
         * A provider polling every few milliseconds with the [TOKEN], failing after [timeout].
         */
        fun fastProvider(timeout: Duration = 1.minutes) =
            CopernicusDataStoreProvider(pollInterval = 10.milliseconds, timeout = timeout) { TOKEN }

        /**
         * A ZIP archive with the given [entries], from their names to their contents.
         */
        fun zipOf(vararg entries: Pair<String, ByteArray>): ByteArray = ByteArrayOutputStream().also { bytes ->
            ZipOutputStream(bytes).use { zip ->
                entries.forEach { (name, content) ->
                    zip.putNextEntry(ZipEntry(name))
                    zip.write(content)
                    zip.closeEntry()
                }
            }
        }.toByteArray()
    }
}
