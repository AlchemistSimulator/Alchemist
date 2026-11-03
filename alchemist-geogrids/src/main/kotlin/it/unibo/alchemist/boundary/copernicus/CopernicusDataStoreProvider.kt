/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import com.google.gson.JsonParser
import it.unibo.alchemist.boundary.filecache.CacheableDataLoader
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
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
 * @param http the HTTP client.
 * @param pollInterval how long to wait before the first poll; it doubles at each poll, up to [maxPollInterval].
 * @param maxPollInterval the longest wait between polls.
 * @param timeout how long the job can take, polls included; the download itself has no time limit.
 * @param tokenSupplier the API token, requested only when downloading.
 */
class CopernicusDataStoreProvider(
    private val http: HttpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT.toJavaDuration()).build(),
    private val pollInterval: Duration = DEFAULT_POLL_INTERVAL,
    private val maxPollInterval: Duration = DEFAULT_MAX_POLL_INTERVAL,
    private val timeout: Duration = DEFAULT_TIMEOUT,
    private val tokenSupplier: () -> String,
) : CacheableDataLoader<CopernicusRequest> {

    init {
        require(pollInterval.isPositive()) { "pollInterval must be positive" }
        require(maxPollInterval >= pollInterval) { "maxPollInterval must be >= than pollInterval" }
        require(timeout.isPositive()) { "timeout must be positive" }
    }

    private val token: String by lazy(tokenSupplier)

    /**
     * Downloads into [target] the data of [key], checks it, and extracts any ZIP archive.
     *
     * @throws IllegalArgumentException if [target] is not a directory.
     * @throws IllegalStateException if the job fails or times out, or the download fails or is corrupted.
     */
    override fun load(key: CopernicusRequest, target: Path) {
        require(target.isDirectory()) {
            "$target is not a directory or does not exist"
        }
        logger.info("Fetching dataset '${key.dataset}'.")
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
        logger.info("Dataset '${key.dataset}' successfully downloaded in $elapsed.")
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
        val monitorUrl = parseMonitorUrl(response.body())
        logger.info("Job submitted for '${request.dataset}', monitoring at $monitorUrl")
        return monitorUrl
    }

    /**
     * Polls the job at [monitorUrl] until it succeeds, waiting [interval] before the next poll and logging every
     * now and then since [lastHeartbeat], returning the URL of its results.
     * Unknown statuses are taken as still running.
     *
     * @throws IllegalStateException if the job fails, or is still running at [deadline].
     */
    private tailrec fun awaitSuccess(
        monitorUrl: String,
        deadline: TimeMark = TimeSource.Monotonic.markNow() + timeout,
        interval: Duration = pollInterval,
        lastHeartbeat: TimeMark = TimeSource.Monotonic.markNow(),
    ): String {
        // fails on timeout
        val timeoutMessage = { "Timeout ($timeout) while waiting for job completion at $monitorUrl" }
        val remaining = -deadline.elapsedNow()
        check(remaining.isPositive(), timeoutMessage)
        // a stalled poll must not outlive the deadline: if it gets cut by it, the job timed out
        val body = try {
            get(monitorUrl, minOf(REQUEST_TIMEOUT, remaining)).body()
        } catch (timedOut: HttpTimeoutException) {
            throw if (deadline.hasPassedNow()) IllegalStateException(timeoutMessage(), timedOut) else timedOut
        }
        return when (val status = parseStatus(body)) {
            SUCCESSFUL -> {
                logger.info("Job completed at $monitorUrl")
                parseResultsUrl(body)
                    ?: error("Job '$SUCCESSFUL' but no rel='results' link at $monitorUrl: inconsistent response")
            }
            in TERMINAL_STATUSES -> failOnStatus(monitorUrl, status, body)
            else -> {
                val heartbeat = if (status in RUNNING_STATUSES) {
                    logger.debug("Job status '$status' at $monitorUrl")
                    // reassures the user that the program is in fact not dead.
                    lastHeartbeat.takeIf { it.elapsedNow() < USER_ALERT_INTERVAL }
                        ?: TimeSource.Monotonic.markNow().also {
                            logger.info("Still waiting for job at $monitorUrl (status: $status)")
                        }
                } else {
                    // warns the user about the new unknown status, but keeps polling.
                    logger.warn("Unrecognized job status '$status' at $monitorUrl, continuing to poll")
                    lastHeartbeat
                }
                // never sleeps past the deadline, so that the next poll fails right on time
                Thread.sleep(minOf(interval, -deadline.elapsedNow()).coerceAtLeast(Duration.ZERO).inWholeMilliseconds)
                awaitSuccess(monitorUrl, deadline, (interval * 2).coerceAtMost(maxPollInterval), heartbeat)
            }
        }
    }

    /**
     * The file to download, from the results at [resultsUrl].
     */
    private fun fetchAsset(resultsUrl: String): RemoteAsset {
        val asset = parseAsset(get(resultsUrl).body())
        val absoluteHref = URI.create(resultsUrl).resolve(asset.href).toString()
        logger.info("Asset will be downloaded from: $absoluteHref (${asset.sizeBytes} bytes)")
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
        logger.info("Downloading $fileName...")
        val request = HttpRequest.newBuilder().uri(uri).GET().build() // no PRIVATE-TOKEN needed
        val response = http.send(request, HttpResponse.BodyHandlers.ofFile(target))
        // http error code check
        if (!response.isSuccessful) {
            error("Download failed (HTTP ${response.statusCode()}) from ${asset.href}")
        }
        logger.info("Downloaded $fileName")
        return response.body()
    }

    /**
     * Fails reporting the [response] to [action], with its error report if any.
     */
    private fun failOnHttpError(action: String, response: HttpResponse<String>): Nothing {
        // problem description, if any
        val described = runCatching {
            parseProblemDetail(response.body()).describe()
        }.getOrNull()
        error(
            // if no description is available, the whole body is shown
            if (described.isNullOrBlank()) {
                "$action failed (HTTP ${response.statusCode()}). Body: ${response.body()}"
            } else {
                "$action failed (HTTP ${response.statusCode()}): $described"
            },
        )
    }

    /**
     * Fails reporting the cause of the job at [monitorUrl] ending with [status]: the data stores explain it in the
     * results, otherwise its `message` or the whole status [body] are reported.
     */
    private fun failOnStatus(monitorUrl: String, status: String, body: String): Nothing {
        val cause = parseResultsUrl(body)
            ?.let { runCatching { parseProblemDetail(getRaw(it).body()).describe() }.getOrNull() }
            ?.takeUnless { it.isBlank() }
            ?: runCatching { parseFailureMessage(body) }.getOrNull()
        error(
            buildString {
                append("Job in state '$status' at $monitorUrl")
                if (cause.isNullOrBlank()) append(". Body: $body") else append(": $cause")
            },
        )
    }

    /**
     * Gets [url] with the token, waiting at most [responseTimeout].
     *
     * @throws IllegalStateException if the response is an error.
     */
    private fun get(url: String, responseTimeout: Duration = REQUEST_TIMEOUT): HttpResponse<String> =
        getRaw(url, responseTimeout).also {
            if (!it.isSuccessful) failOnHttpError("GET $url", it)
        }

    /**
     * Gets [url] with the token, waiting at most [responseTimeout], whatever the response.
     */
    private fun getRaw(url: String, responseTimeout: Duration = REQUEST_TIMEOUT): HttpResponse<String> = http.send(
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("PRIVATE-TOKEN", token)
            .header("Accept", APPLICATION_JSON)
            .timeout(responseTimeout.toJavaDuration())
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString(),
    )

    /**
     * A file to download from [href], with its size in bytes and, if advertised, its MD5 digest
     * as the data store wrote it (see [parseAsset]).
     */
    private data class RemoteAsset(val href: String, val sizeBytes: Long, val md5: String?)

    /**
     * An [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) error report of the data store, with its trace identifier
     * and, for a failed job, the [traceback] of the backend, often the only field with the actual cause.
     */
    private data class ProblemDetail(
        val type: String,
        val title: String?,
        val detail: String?,
        val traceId: String?,
        val traceback: String?,
    ) {
        /**
         * A human-readable description, empty if there is nothing to describe.
         */
        fun describe(): String {
            val core = detail ?: traceback ?: title ?: return ""
            return buildString {
                append(core)
                append(" [type=$type")
                traceId?.let { append(", trace=$it") }
                append("]")
            }
        }
    }

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
         * The default longest wait between polls, as in ECMWF's official client.
         */
        private val DEFAULT_MAX_POLL_INTERVAL = 120.seconds

        /**
         * How often to log that the job is still running.
         */
        private val USER_ALERT_INTERVAL = 30.seconds

        /**
         * How long to wait for a connection.
         */
        private val CONNECT_TIMEOUT = 30.seconds

        /**
         * How long to wait for a response of the API.
         */
        private val REQUEST_TIMEOUT = 1.minutes

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
         * Parsers of the JSON bodies of the API of the Copernicus data stores, which follows OGC API - Processes
         * (https://docs.ogc.org/is/18-062r2/18-062r2.html) loosely: they read only the fields needed.
         */

        /**
         * The `href` of the first entry in the `links` of [json] with the given [rel], if any.
         */
        private fun linkHref(json: String, rel: String): String? = JsonParser.parseString(json)
            .asJsonObject.getAsJsonArray("links")
            ?.map { it.asJsonObject }
            ?.firstOrNull { it.get("rel")?.asString == rel }
            ?.get("href")?.asString

        /**
         * The file to download, from the results response [json].
         * The MD5 digest is kept as written, without the leading zeros the data store drops.
         *
         * @throws IllegalStateException if the URL or the size of the file is missing.
         */
        private fun parseAsset(json: String): RemoteAsset {
            val value = JsonParser.parseString(json).asJsonObject
                .getAsJsonObject("asset")
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
         * The `message` of the status response [json], if any.
         * The data stores currently leave it empty, and explain failures in the results instead.
         */
        private fun parseFailureMessage(json: String): String? = JsonParser.parseString(json)
            .asJsonObject.get("message")?.takeUnless { it.isJsonNull }?.asString

        /**
         * The URL to monitor the job, from the response [json] to its submission.
         *
         * @throws IllegalStateException if there is none.
         */
        private fun parseMonitorUrl(json: String): String =
            linkHref(json, "monitor") ?: error("No link with rel='monitor' in the submit response")

        /**
         * The error report in [json]. Its `type` is not always a URI, so it is kept as a plain string.
         */
        private fun parseProblemDetail(json: String): ProblemDetail {
            val obj = JsonParser.parseString(json).asJsonObject

            fun field(name: String) = obj.get(name)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
            return ProblemDetail(
                type = field("type")?.asString ?: "about:blank",
                title = field("title")?.asString,
                detail = field("detail")?.asString,
                traceId = field("trace_id")?.asString,
                traceback = field("traceback")?.asString,
            )
        }

        /**
         * The URL of the results of the job, from the status response [json]; `null` until the job ends.
         * For a failed job, it answers with an error report (see [parseProblemDetail]) explaining the failure.
         */
        private fun parseResultsUrl(json: String): String? = linkHref(json, "results")

        /**
         * The status of the job in the status response [json], as a string: the data store may add new ones.
         *
         * @throws IllegalStateException if there is none.
         */
        private fun parseStatus(json: String): String = JsonParser.parseString(json)
            .asJsonObject.get("status")
            ?.asString
            ?: error("No 'status' field in the status response")

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
