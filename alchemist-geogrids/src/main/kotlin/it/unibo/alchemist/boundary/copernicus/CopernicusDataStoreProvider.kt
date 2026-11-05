/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import it.unibo.alchemist.boundary.copernicus.CopernicusDataStoreProvider.Companion.asset
import it.unibo.alchemist.boundary.filecache.CacheableDataLoader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.isDirectory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlin.time.toJavaDuration
import okio.ByteString.Companion.decodeHex
import okio.FileSystem
import okio.HashingSource.Companion.md5
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import okio.blackholeSink
import okio.buffer
import okio.openZip
import org.slf4j.LoggerFactory

/**
 * Downloads data from a Copernicus data store: it submits a job, polls it until done, then downloads its results.
 *
 * @param pollInterval how long to wait before the first poll; it doubles at each poll, up to two minutes,
 * as in ECMWF's official client.
 * @param timeout how long the job can take, polls included; the download itself has no time limit.
 * @param tokenSupplier the API token, requested only when downloading.
 */
class CopernicusDataStoreProvider(
    private val pollInterval: Duration = DEFAULT_POLL_INTERVAL,
    private val timeout: Duration = DEFAULT_TIMEOUT,
    private val tokenSupplier: () -> String,
) : CacheableDataLoader<CopernicusRequest> {

    init {
        require(pollInterval.isPositive() && pollInterval <= MAX_POLL_INTERVAL) {
            "pollInterval must be positive and at most $MAX_POLL_INTERVAL"
        }
        require(timeout.isPositive()) { "timeout must be positive" }
    }

    private val token: String by lazy(tokenSupplier)

    /**
     * Downloads into [target] the data of [key], checks it, and extracts any ZIP archive.
     *
     * @throws IllegalArgumentException if [target] is not a directory.
     * @throws IllegalStateException if the job fails or times out, or the download fails or is corrupted.
     * @throws java.io.IOException if the connection fails or times out, or a downloaded archive cannot be read.
     */
    override fun load(key: CopernicusRequest, target: Path) {
        require(target.isDirectory()) {
            "$target is not a directory or does not exist"
        }
        val start = TimeSource.Monotonic.markNow()
        // asks ECMWF servers to process the data.
        val monitorUrl = submit(key)
        // polls until the data can be retrieved.
        val resultsUrl = awaitSuccess(monitorUrl)
        // retrieves the URI to the produced asset.
        val asset = fetchAsset(resultsUrl)
        // downloads the asset.
        val file = download(asset, target)
        // asset validation and archive flattening.
        file.verify(asset.sizeBytes, asset.md5)
        target.extractAndDeleteAllZipArchives()
        val elapsed = start.elapsedNow().inWholeMilliseconds.milliseconds
        logger.info("Dataset '${key.dataset}' downloaded in $elapsed (${asset.sizeBytes} bytes).")
    }

    /**
     * Submits the job for [request], returning the URL to monitor it.
     */
    private fun submit(request: CopernicusRequest): String {
        val body = CanonicalJson.encode(mapOf("inputs" to request.inputs))
        // builds the full POST request
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("${request.endpoint}/retrieve/v1/processes/${request.dataset}/execution"))
            // always needed! A 403 error would be thrown otherwise
            .header("PRIVATE-TOKEN", token)
            .header("Content-Type", APPLICATION_JSON)
            .header("Accept", APPLICATION_JSON)
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .timeout(REQUEST_TIMEOUT.toJavaDuration())
            .build()
        val response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString())
        // something has gone wrong
        if (!response.isSuccessful) {
            failOnHttpError("Submit of dataset '${request.dataset}'", response)
        }
        val monitorUrl = response.body().toJsonObject().link("monitor")
            ?: error("No link with rel='monitor' in the submit response")
        logger.info("Job submitted for '${request.dataset}', monitoring at $monitorUrl")
        return monitorUrl
    }

    /**
     * Polls the job at [monitorUrl] until it succeeds, waiting [interval] before the next poll, and returns the URL of
     * its results.
     * Unknown statuses are taken as still running; statuses are logged when they differ from [previousStatus].
     *
     * @throws IllegalStateException if the job fails, or is still running at [deadline].
     */
    private tailrec fun awaitSuccess(
        monitorUrl: String,
        deadline: TimeMark = TimeSource.Monotonic.markNow() + timeout,
        interval: Duration = pollInterval,
        previousStatus: String? = null,
    ): String {
        check(deadline.hasNotPassedNow()) { "Timeout ($timeout) while waiting for job completion at $monitorUrl" }
        val job = get(monitorUrl).body().toJsonObject()
        return when (val status = job.status) {
            SUCCESSFUL -> job.link("results")
                ?: error("Job '$SUCCESSFUL' but no rel='results' link at $monitorUrl: inconsistent response")
            in TERMINAL_STATUSES -> failOnStatus(monitorUrl, status, job)
            else -> {
                if (status != previousStatus) {
                    if (status in RUNNING_STATUSES) {
                        logger.info("Job status '$status' at $monitorUrl")
                    } else {
                        logger.warn("Unrecognized job status '$status' at $monitorUrl, continuing to poll")
                    }
                }
                // sleeps at most until just past the deadline, so that the next poll fails right on time:
                // rounded up, as waking up just before the deadline would send one more request
                val remaining = minOf(interval, -deadline.elapsedNow()).coerceAtLeast(Duration.ZERO)
                Thread.sleep(remaining.inWholeMilliseconds + 1)
                awaitSuccess(monitorUrl, deadline, (interval * 2).coerceAtMost(MAX_POLL_INTERVAL), status)
            }
        }
    }

    /**
     * The file to download, from the results at [resultsUrl].
     */
    private fun fetchAsset(resultsUrl: String): RemoteAsset {
        val asset = get(resultsUrl).body().toJsonObject().asset
        val absoluteHref = URI.create(resultsUrl).resolve(asset.href).toString()
        // the download can take long, so the user is told when it starts
        logger.info("Job completed, downloading ${asset.sizeBytes} bytes from $absoluteHref")
        return asset.copy(href = absoluteHref)
    }

    /**
     * Downloads [asset] into [targetDir]. Assets are public, so the token is not sent.
     */
    private fun download(asset: RemoteAsset, targetDir: Path): Path {
        val uri = URI.create(asset.href)
        // extracts the asset name from the uri
        val fileName = uri.path.substringAfterLast('/').ifEmpty { "download" }
        val target = targetDir.resolve(fileName)
        val request = HttpRequest.newBuilder().uri(uri).GET().build() // no PRIVATE-TOKEN needed
        val response = http.send(request, HttpResponse.BodyHandlers.ofFile(target))
        // http error code check
        if (!response.isSuccessful) {
            error("Download failed (HTTP ${response.statusCode()}) from ${asset.href}")
        }
        return response.body()
    }

    /**
     * Fails reporting the [response] to [action], with its error report if any.
     */
    private fun failOnHttpError(action: String, response: HttpResponse<String>): Nothing {
        val described = response.body().toJsonObjectOrNull()?.problem
        error("$action failed (HTTP ${response.statusCode()}). ${described ?: "Body: ${response.body()}"}")
    }

    /**
     * Fails reporting the cause of the [job] at [monitorUrl] ending with [status]: the data stores explain it in the
     * results, otherwise its `message` or its whole status are reported.
     */
    private fun failOnStatus(monitorUrl: String, status: String, job: JsonObject): Nothing {
        val cause = job.link("results")?.let { get(it, acceptErrors = true).body().toJsonObjectOrNull()?.problem }
            ?: job.message?.takeUnless { it.isBlank() }
        error("Job in state '$status' at $monitorUrl${if (cause == null) ". Body: $job" else ": $cause"}")
    }

    /**
     * Gets [url] with the token.
     *
     * @throws IllegalStateException if the response is an error, unless [acceptErrors].
     */
    private fun get(url: String, acceptErrors: Boolean = false): HttpResponse<String> = http.send(
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("PRIVATE-TOKEN", token)
            .header("Accept", APPLICATION_JSON)
            .timeout(REQUEST_TIMEOUT.toJavaDuration())
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString(),
    ).also { if (!acceptErrors && !it.isSuccessful) failOnHttpError("GET $url", it) }

    /**
     * A file to download from [href], with its size in bytes and, if advertised, its MD5 digest
     * as the data store wrote it (see [asset]).
     */
    private data class RemoteAsset(val href: String, val sizeBytes: Long, val md5: String?)

    internal companion object {
        /**
         * How long a job can take by default.
         */
        val DEFAULT_TIMEOUT = 30.minutes

        /**
         * The default wait before the first poll.
         */
        private val DEFAULT_POLL_INTERVAL = 2.seconds

        /**
         * The longest wait between polls, as in ECMWF's official client.
         */
        private val MAX_POLL_INTERVAL = 120.seconds

        /**
         * How long to wait for a connection.
         */
        private val CONNECT_TIMEOUT = 30.seconds

        /**
         * How long to wait for a response of the API.
         */
        private val REQUEST_TIMEOUT = 1.minutes

        /**
         * The HTTP client, shared by all the providers.
         */
        private val http: HttpClient by lazy {
            HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT.toJavaDuration()).build()
        }

        private const val APPLICATION_JSON = "application/json"

        private const val SUCCESSFUL = "successful"

        private const val MD5_HEX_DIGITS = 32

        private val TERMINAL_STATUSES = setOf("failed", "rejected", "dismissed", "deleted")

        private val RUNNING_STATUSES = setOf("accepted", "running")

        private val logger = LoggerFactory.getLogger(CopernicusDataStoreProvider::class.java)

        /**
         * How ZIP archives start, also when empty.
         */
        private val zipSignatures = listOf("504b0304", "504b0506").map { it.decodeHex() }

        /*
         * Readers of the JSON bodies of the API of the Copernicus data stores, which follows OGC API - Processes
         * (https://docs.ogc.org/is/18-062r2/18-062r2.html) loosely: they read only the fields needed.
         */

        /**
         * The file to download, from this results response.
         * The MD5 digest is kept as written, without the leading zeros the data store drops.
         *
         * @throws IllegalStateException if the URL or the size of the file is missing.
         */
        private val JsonObject.asset: RemoteAsset get() {
            val value = getAsJsonObject("asset")
                ?.getAsJsonObject("value")
                ?: error("No 'asset.value' object in the results response")
            fun field(name: String) = value.get(name)?.takeUnless { it.isJsonNull }
            return RemoteAsset(
                href = field("href")?.asString ?: error("No 'href' in asset.value"),
                sizeBytes = field("file:size")?.asLong ?: error("No 'file:size' in asset.value"),
                md5 = field("file:checksum")?.asString,
            )
        }

        /**
         * The `message` of this status response, if any.
         * The data stores currently leave it empty, and explain failures in the results instead.
         */
        private val JsonObject.message: String? get() = get("message")?.takeUnless { it.isJsonNull }?.asString

        /**
         * The description of this [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) error report, `null` if it has
         * none: its detail, or, for a failed job, the traceback of the backend, often the only field with the actual
         * cause, or its title; followed by its type, not always a URI, and its trace identifier.
         */
        private val JsonObject.problem: String? get() {
            fun field(name: String) = get(name)?.takeIf { it.isJsonPrimitive }?.asString?.takeUnless { it.isBlank() }
            val trace = field("trace_id")?.let { ", trace=$it" }.orEmpty()
            return (field("detail") ?: field("traceback") ?: field("title"))
                ?.let { "$it [type=${field("type") ?: "about:blank"}$trace]" }
        }

        /**
         * The status of the job in this status response, as a string: the data store may add new ones.
         *
         * @throws IllegalStateException if there is none.
         */
        private val JsonObject.status: String get() = get("status")?.asString
            ?: error("No 'status' field in the status response")

        /**
         * The `href` of the first entry in the `links` of this response with the given [rel], if any:
         * `monitor` for the job of a submission, `results` for the results of a job that ended.
         */
        private fun JsonObject.link(rel: String): String? = getAsJsonArray("links")
            ?.map { it.asJsonObject }
            ?.firstOrNull { it.get("rel")?.asString == rel }
            ?.get("href")?.asString

        private fun String.toJsonObject(): JsonObject = JsonParser.parseString(this).asJsonObject

        /**
         * This string as a JSON object, `null` if it is not one, as error bodies may be plain text.
         */
        private fun String.toJsonObjectOrNull(): JsonObject? = runCatching {
            JsonParser.parseString(this).takeIf { it.isJsonObject }?.asJsonObject
        }.getOrNull()

        /**
         * Extracts in this directory the entries of all its ZIP archives, flattened, and deletes the archives.
         *
         * @throws IllegalStateException if two entries have the same name.
         */
        private fun Path.extractAndDeleteAllZipArchives() {
            val directory = toOkioPath()
            FileSystem.SYSTEM.list(directory)
                .filter { FileSystem.SYSTEM.metadata(it).isRegularFile && it.isZip() }
                .forEach { archive ->
                    archive.extractTo(directory)
                    FileSystem.SYSTEM.delete(archive)
                }
        }

        private fun okio.Path.extractTo(dir: okio.Path) {
            FileSystem.SYSTEM.openZip(this).use { zip ->
                zip.listRecursively("/".toPath())
                    .filter { zip.metadata(it).isRegularFile }
                    .forEach { entry ->
                        val target = dir / entry.name
                        check(!FileSystem.SYSTEM.exists(target)) {
                            "Flatten collision on '${target.name}' from '$name'"
                        }
                        FileSystem.SYSTEM.write(target, mustCreate = true) {
                            zip.source(entry).use { writeAll(it) }
                        }
                    }
            }
        }

        /**
         * The MD5 digest of this file, as 32 lowercase hexadecimal digits.
         */
        private val Path.md5Hex: String get() = md5(FileSystem.SYSTEM.source(toOkioPath())).use { source ->
            source.buffer().readAll(blackholeSink())
            source.hash.hex()
        }

        /**
         * Whether the response is not an error.
         */
        private val HttpResponse<*>.isSuccessful: Boolean
            get() = statusCode() in 200..299

        /**
         * Checks that this file has [expectedSizeBytes] bytes and, if [expectedMd5] is given, that digest.
         * The data store advertises digests without their leading zeros, so [expectedMd5] is padded back before
         * comparing.
         *
         * @throws IllegalStateException on a mismatch, or if [expectedMd5] is not an MD5 digest.
         */
        private fun Path.verify(expectedSizeBytes: Long, expectedMd5: String?) {
            val actualSize = fileSize()
            check(actualSize == expectedSizeBytes) {
                "Size mismatch for '$fileName': expected $expectedSizeBytes bytes, got $actualSize"
            }
            // no checksum advertised: nothing to verify.
            val advertised = expectedMd5 ?: return
            val expected = advertised.lowercase().padStart(MD5_HEX_DIGITS, '0')
            check(expected.isMd5Hex()) {
                "Data store advertised an unusable MD5 checksum ($advertised) for '$fileName'"
            }
            val actual = md5Hex
            check(actual == expected) {
                "MD5 mismatch for '$fileName': expected $expected (advertised as '$advertised'), got $actual"
            }
        }

        /**
         * Whether this file is a ZIP archive, whatever its extension.
         */
        private fun okio.Path.isZip(): Boolean = FileSystem.SYSTEM.source(this).buffer().use { source ->
            zipSignatures.any { source.rangeEquals(0, it) }
        }

        private fun String.isMd5Hex(): Boolean = length == MD5_HEX_DIGITS && all { it in '0'..'9' || it in 'a'..'f' }
    }
}
