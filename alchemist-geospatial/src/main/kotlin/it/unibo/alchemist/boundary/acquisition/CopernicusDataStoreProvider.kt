/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.acquisition

import it.unibo.alchemist.boundary.utils.CanonicalJson
import it.unibo.alchemist.boundary.utils.RemoteAsset
import it.unibo.alchemist.boundary.utils.flattenArchives
import it.unibo.alchemist.boundary.utils.parseAsset
import it.unibo.alchemist.boundary.utils.parseFailureMessage
import it.unibo.alchemist.boundary.utils.parseMonitorUrl
import it.unibo.alchemist.boundary.utils.parseProblemDetail
import it.unibo.alchemist.boundary.utils.parseResultsUrl
import it.unibo.alchemist.boundary.utils.parseStatus
import it.unibo.alchemist.boundary.utils.verify
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import org.slf4j.LoggerFactory

/**
 * The **only point of the module that speaks the ECMWF data stores' REST API**.
 *
 * Implements the OGC API - Processes flow (submit -> poll -> results -> download) and confines it
 * entirely here.
 *
 * The sole auth asymmetry is the final download GET, which carries **no** token
 * (the asset lives on a public object store on a different host): see [download].
 *
 * @param checkMd5 whether to check the MD5 digest of the downloaded file. Sometimes Copernicus stores
 * return the correct requested assets but report an incorrect MD5, so it may be useful to
 * disable this check.
 * @param tokenSupplier supplies the ECMWF token sent as the `PRIVATE-TOKEN` header on the OGC GET/POST calls.
 * @param http HTTP client to use; defaults to the JDK [HttpClient].
 * @param pollInterval base interval between two status polls; grows with backoff up to [maxPollInterval].
 * @param maxPollInterval cap on the polling interval. Defaults to 120 seconds, matching the official
 * ECMWF client's duration.
 * @param timeout overall guillotine on the wait for job completion.
 */
class CopernicusDataStoreProvider(
    private val checkMd5: Boolean = true,
    private val http: HttpClient = HttpClient.newHttpClient(),
    private val pollInterval: Duration = DEFAULT_POLL_INTERVAL,
    private val maxPollInterval: Duration = DEFAULT_MAX_POLL_INTERVAL,
    private val timeout: Duration = DEFAULT_TIMEOUT,
    private val tokenSupplier: () -> String,
) : ExternalDataProvider<CopernicusRequest> {

    init {
        require(pollInterval.isPositive()) { "pollInterval must be positive" }
        require(maxPollInterval >= pollInterval) { "maxPollInterval must be >= than pollInterval" }
        require(timeout.isPositive()) { "timeout must be positive" }
    }

    /**
     * read on first use, not at construction: a cache hit must not require credentials,
     * so the token can be absent.
     */
    private val token: String by lazy(tokenSupplier)

    /**
     * The full OGC API processes flow, written into [targetDir].
     * Follows the flow submit -> poll -> results -> download, **blocking by design**.
     * Downloaded assets are verified against their size and, optionally, their MD5.
     * Archives are flattened into single files.
     *
     * @param request the [CopernicusRequest] used to initialize the job on ECMWF servers.
     * @param targetDir the temporary directory where the downloaded assets will be saved.
     * @throws IllegalArgumentException if [targetDir] does not exist or is not a directory.
     * @throws IllegalStateException if the assets can't be downloaded, on timeout or if the
     * asset validation fails.
     */
    override fun fetch(request: CopernicusRequest, targetDir: Path) {
        require(targetDir.isDirectory()) {
            "$targetDir is not a directory or does not exist"
        }
        logger.info("Fetching dataset '${request.dataset}'.")
        val start = TimeSource.Monotonic.markNow()
        // asks ECMWF servers to process the data.
        val monitorUrl = submit(request)
        // polls until the data can be retrieved.
        val resultsUrl = awaitSuccess(monitorUrl)
        // retrieves the URI to the produced asset.
        val asset = fetchAsset(resultsUrl)
        // downloads the asset.
        val file = download(asset, targetDir)
        // asset validation and archive flattening.
        if (!checkMd5) {
            logger.warn("MD5 check disabled. Only the size in bytes is checked.")
        }
        verify(file, asset.sizeBytes, asset.takeIf { checkMd5 }?.md5) { md5, file ->
            check(!checkMd5) {
                "Data store advertised an unusable MD5 checksum ($md5) for '$file': could not check asset integrity."
            }
        }
        flattenArchives(targetDir)
        val elapsed = start.elapsedNow().inWholeMilliseconds.milliseconds
        logger.info("Dataset '${request.dataset}' successfully downloaded in $elapsed.")
    }

    /**
     * Submits the job and returns the URL from which to monitor its status.
     *
     * `POST {endpoint}/retrieve/v1/processes/{dataset}/execution`, with `PRIVATE-TOKEN` and
     * `Content-Type`/`Accept: application/json`, body `{"inputs": <request.inputs>}` serialized via
     * [CanonicalJson]. The monitor URL is read from the `rel="monitor"` link of the response
     * ([parseMonitorUrl]), never rebuilt from a path.
     *
     * @param request the [CopernicusRequest] used to initialize the job on ECMWF servers.
     * @return the absolute job URL to pass to [awaitSuccess].
     * @throws IllegalStateException on a non-2xx response, enriched with [parseProblemDetail].
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
     * **Polls** the job status at [monitorUrl] until it becomes `successful`, with exponential
     * backoff capped at [maxPollInterval] and a [timeout] guillotine.
     *
     * Terminal failure states are listed **explicitly** (`failed`/`rejected`/`dismissed`):
     * consistently with [parseStatus], any other state (known like `accepted`/`running` or unknown),
     * is treated as transient and polling continues.
     *
     * @param monitorUrl the URL to use to check the job status. See [submit].
     * @return the result URL (`rel="results"` link, present only once the job is `successful`).
     * @throws IllegalStateException on a terminal failure state, on [timeout], or if a `successful`
     * job exposes no `rel="results"` link (inconsistent server response).
     */
    private fun awaitSuccess(monitorUrl: String): String {
        // the last available moment to check for a successful poll.
        val deadline = TimeSource.Monotonic.markNow() + timeout
        var interval = pollInterval
        /*
         * tracks the last time the user was alerted that the program is still
         * polling (to prevent them from thinking it has frozen).
         */
        var lastHeartbeat = TimeSource.Monotonic.markNow()
        // tries to poll until a success/timeout/error
        while (true) {
            val body = get(monitorUrl).body()
            // status check
            when (val status = parseStatus(body)) {
                "successful" -> {
                    logger.info("Job completed at $monitorUrl")
                    return parseResultsUrl(body)
                        ?: error("Job 'successful' but no rel='results' link at $monitorUrl: inconsistent response")
                }
                in TERMINAL_STATUSES -> failOnStatus(monitorUrl, status, body)
                in RUNNING_STATUSES -> {
                    // fine details on debug mode
                    logger.debug("Job status '$status' at $monitorUrl")
                    // reassures the user that the program is in fact not dead.
                    if (lastHeartbeat.elapsedNow() >= USER_ALERT_INTERVAL) {
                        logger.info("Still waiting for job at $monitorUrl (status: $status)")
                        lastHeartbeat = TimeSource.Monotonic.markNow()
                    }
                }
                // warns the user about the new unknow status, but keeps polling.
                else -> logger.warn("Unrecognized job status '$status' at $monitorUrl, continuing to poll")
            }
            // fails on timeout
            check(deadline.hasNotPassedNow()) {
                "Timeout ($timeout) while waiting for job completion at $monitorUrl"
            }
            Thread.sleep(interval.inWholeMilliseconds)
            interval = (interval * 2).coerceAtMost(maxPollInterval)
        }
    }

    /**
     * Fetches the result metadata. `GET resultUrl` (authenticated).
     *
     * Extracts href, size and MD5 via [parseAsset], then resolves the href against [resultsUrl].
     * ECMWF already serves the asset URI as absolute, so the resolve is a defensive no-op
     * (tolerates a future relative href).
     *
     * @param resultsUrl the results URL from [awaitSuccess] (`rel="results"` link).
     * @return a [RemoteAsset] with an **absolute** href, expected size, and best-effort checksum.
     */
    private fun fetchAsset(resultsUrl: String): RemoteAsset {
        val asset = parseAsset(get(resultsUrl).body())
        val absoluteHref = URI.create(resultsUrl).resolve(asset.href).toString()
        logger.info("Asset will be downloaded from: $absoluteHref (${asset.sizeBytes} bytes)")
        return asset.copy(href = absoluteHref)
    }

    /**
     * Streams the asset file into [targetDir].
     *
     * `GET asset.href` **without** `PRIVATE-TOKEN`: unlike the OGC GETs (monitor, results), the
     * href points to an object store on a different host (e.g. `object-store.os-api.cci2.ecmwf.int`)
     * that serves the resource **publicly, unauthenticated**.
     *
     * @param asset result metadata (absolute href, expected size, nullable checksum).
     * @param targetDir temporary directory to write into.
     * @return the [Path] of the downloaded asset.
     * @throws IllegalStateException on a non-2xx response from the object store (it does not
     * follow RFC 7807, so only the status is reported).
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
     * Always throws as [IllegalStateException], enriching the message with the RFC 7807 problem-detail
     * **when** the body is interpretable.
     *
     * @param action the action performed that caused the error.
     * @param response the [HttpResponse] that caused the error.
     * @throws IllegalStateException always.
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
     * Always throws for a terminal failure job (HTTP 200 with a `failed`/`rejected`/`dismissed` status).
     *
     * **Note**: the status document does **not** carry the cause: ECMWF exposes it only by showing the
     * `rel="results"` link, which answers 4xx with an RFC 7807 body whose proprietary `traceback`
     * field holds the backend failure. The OGC `message` field and the raw status body are
     * the fallbacks, **in that order**.
     *
     * @param monitorUrl the poll URL that reported a terminal failure status.
     * @param status the terminal failure status (`failed`/`rejected`/`dismissed`).
     * @param body the status document, used to locate the results link and, failing that, as a fallback
     * message source.
     * @throws IllegalStateException always.
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
     * Authenticated GET (with `PRIVATE-TOKEN` header) to an OGC endpoint.
     *
     * @param url the request URL.
     * @return the [HttpResponse] received after the request.
     * @throws IllegalStateException on a non-2xx response.
     */
    private fun get(url: String): HttpResponse<String> = getRaw(url).also {
        if (!it.isSuccessful) failOnHttpError("GET $url", it)
    }

    /**
     * Authenticated GET that does **not** check the status code: the caller inspects the body.
     */
    private fun getRaw(url: String): HttpResponse<String> = http.send(
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("PRIVATE-TOKEN", token)
            .header("Accept", APPLICATION_JSON)
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString(),
    )

    internal companion object {
        /**
         * Default maximum time allowed to wait for a poll with a ‘successful’ status.
         */
        val DEFAULT_TIMEOUT = 30.minutes

        /**
         * Default interval between two consecutive polls.
         */
        private val DEFAULT_POLL_INTERVAL = 2.seconds

        /**
         * Default max interval between two consecutive polls.
         */
        private val DEFAULT_MAX_POLL_INTERVAL = 120.seconds

        /**
         * How often to alert the user that the program is still in
         * polling mode (to prevent them from thinking the program
         * has frozen).
         */
        private val USER_ALERT_INTERVAL = 30.seconds

        private const val APPLICATION_JSON = "application/json"

        private val TERMINAL_STATUSES = setOf("failed", "rejected", "dismissed", "deleted")

        private val RUNNING_STATUSES = setOf("accepted", "running")

        private val logger = LoggerFactory.getLogger(CopernicusDataStoreProvider::class.java)
    }
}

/**
 * Extension property used to check if a response was successful.
 */
private val HttpResponse<*>.isSuccessful: Boolean
    get() = statusCode() in 200..299
