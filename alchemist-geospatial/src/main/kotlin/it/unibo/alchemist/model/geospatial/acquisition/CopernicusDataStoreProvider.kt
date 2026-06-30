/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.time.Duration
import org.slf4j.LoggerFactory

/**
 * The **only point of the module that speaks the ECMWF data stores' REST API**.
 *
 * Implements the OGC API - Processes flow (submit -> poll -> results -> download) and confines it
 * entirely here: ECMWF marks this API as "recommended for advanced users, not supported" and
 * evolving, so any change to the REST surface impacts **this class alone**.
 *
 * Serves multiple data stores (CDS, ADS, EWDS) indistinctly: the same ECMWF software
 * sits underneath, with identical sub-paths; only the host differs, supplied via [endpoint].
 * The sole auth asymmetry is the final download GET, which carries **no** token
 * (the asset lives on a public object store on a different host): see [download].
 *
 * Network calls are **blocking** by design: the layer's YAML constructor blocks on the first
 * download, after which the cache makes reruns instant.
 *
 * @param endpoint base URL of the data store (e.g. `https://ewds.climate.copernicus.eu/api`); a
 * trailing slash, if present, is trimmed.
 * @param token ECMWF token sent as the `PRIVATE-TOKEN` header on the OGC GET/POST calls.
 * @param cache cache manager: decides hit/miss and guarantees the atomic rename.
 * @param http injectable HTTP client (to make this class testable); defaults to the JDK [HttpClient].
 * @param pollInterval base interval between two status polls; grows with backoff up to [maxPollInterval].
 * @param maxPollInterval cap on the polling interval. Defaults to 120 seconds, matching the official
 * ECMWF client's duration.
 * @param timeout overall guillotine on the wait for job completion.
 */
class CopernicusDataStoreProvider(
    endpoint: String,
    private val token: String,
    private val cache: CacheManager,
    private val http: HttpClient = HttpClient.newHttpClient(),
    private val pollInterval: Duration = Duration.ofSeconds(2),
    private val maxPollInterval: Duration = Duration.ofSeconds(120),
    private val timeout: Duration = Duration.ofMinutes(30),
) : ExternalDataProvider<CopernicusRequest> {

    /**
     * endpoint normalized once: no trailing slash, so path concatenation never yields `//`
     */
    private val base: String = endpoint.trimEnd('/')

    /**
     * Returns the cache directory for [request], retrieving the asset only on cache miss.
     *
     * @param request the [CopernicusRequest] that will be used to determine a cache hit/miss.
     * @return the path to the cache entry, filled with data.
     *
     * @throws IllegalStateException if no file is produced on cache miss.
     */
    override fun fetch(request: CopernicusRequest): Path = cache.getOrProduce(request) { targetDir ->
        this.produce(request, targetDir)
    }

    /**
     * The full OGC API processes flow, written into [targetDir].
     * Follows the flow submit -> poll -> results -> download, **blocking by design**.
     * Downloaded assets are verified against their size and, optionally, their MD5.
     * Archives are flattened into single files.
     *
     * @param request the [CopernicusRequest] used to initialize the job on ECMWF servers.
     * @param targetDir the temporary directory where the downloaded assets will be saved.
     *
     * @throws IllegalStateException if the assets can't be downloaded or on timeout.
     */
    private fun produce(request: CopernicusRequest, targetDir: Path) {
        // asks ECMWF servers to elaborate the data.
        val monitorUrl = submit(request)
        // polls until the data can be retrieved.
        val resultsUrl = awaitSuccess(monitorUrl)
        // retrieves the URI to the produced asset.
        val asset = fetchAsset(resultsUrl)
        // downloads the asset.
        val file = download(asset, targetDir)
        // asset validation and optional archive flattening.
        verify(file, asset.sizeBytes, asset.md5)
        flattenArchives(targetDir)
    }

    /**
     * Submits the job and returns the URL from which to monitor its status.
     *
     * `POST {endpoint}/retrieve/v1/processes/{dataset}/execute`, with `PRIVATE-TOKEN` and
     * `Content-Type`/`Accept: application/json`, body `{"inputs": <request.inputs>}` serialized via
     * [CanonicalJson]. The monitor URL is read from the `rel="monitor"` link of the response
     * ([parseMonitorUrl]), never rebuilt from a path.
     *
     * @param request the [CopernicusRequest] used to initialize the job on ECMWF servers.
     * @return the absolute job URL to pass to [awaitSuccess].
     *
     * @throws IllegalStateException on a non-2xx response, enriched with [parseProblemDetail].
     */
    private fun submit(request: CopernicusRequest): String {
        val body = CanonicalJson.encode(mapOf("inputs" to request.inputs))

        // builds the full POST request
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$base/retrieve/v1/processes/${request.dataset}/execute"))
            // always needed! A 403 error would be thrown otherwise
            .header("PRIVATE-TOKEN", token)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()

        val response = http.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        // something has gone wrong
        if (response.statusCode() !in 200..299) {
            failOnHttpError("Submit of dataset '${request.dataset}'", response)
        }
        return parseMonitorUrl(response.body())
    }

    /**
     * **Polls** the job status at [monitorUrl] until it becomes `successful`, with exponential
     * backoff capped at [maxPollInterval] and a [timeout] guillotine.
     *
     * Terminal failure states are listed **explicitly** (`failed`/`rejected`/`dismissed`):
     * consistently with [parseStatus], any other state (known like `accepted`/`running` or unknown),
     * is treated as transient and polling continues.
     *
     * **Note**: a *future, unannounced* terminal state would be awaited up to [timeout] rather than
     * detected at once. This is tolerable bacause [timeout] still guarantees termination, and
     * unrecognized states are logged at WARN for diagnosis.
     *
     * @param monitorUrl the URL to use to check the job status. See [submit].
     * @return the result URL (`rel="results"` link, present only once the job is `successful`).
     *
     * @throws IllegalStateException on a terminal failure state, on [timeout], or if a `successful`
     * job exposes no `rel="results"` link (inconsistent server response).
     */
    private fun awaitSuccess(monitorUrl: String): String {
        // the last available moment to check for a successful poll.
        val deadline = System.nanoTime() + timeout.toNanos()
        var interval = pollInterval

        /*
         * how often to alert the user that the program is still in
         * polling mode (to prevent them from thinking the program
         * has frozen).
         */
        val heartbeatNanos = Duration.ofSeconds(30).toNanos()
        var lastHeartbeat = System.nanoTime()

        // tries to poll until a success/timeout/error
        while (true) {
            val body = get(monitorUrl).body()

            // status check
            when (val status = parseStatus(body)) {
                "successful" -> {
                    logger.info("Job completed at {}", monitorUrl)
                    return parseResultsUrl(body)
                        ?: error("Job 'successful' but no rel='results' link at $monitorUrl: inconsistent response")
                }
                "failed", "rejected", "dismissed" -> failOnStatus(monitorUrl, status, body)
                "accepted", "running" -> {
                    // fine details on debug mode
                    logger.debug("Job status '{}' at {}", status, monitorUrl)
                    val now = System.nanoTime()

                    // reassures the user that the program is in fact not dead.
                    if (now - lastHeartbeat >= heartbeatNanos) {
                        logger.info("Still waiting for job at {} (status: {})", monitorUrl, status)
                        lastHeartbeat = now
                    }
                }
                // warns the user about the new unknow status, but keeps polling
                else -> logger.warn("Unrecognized job status '{}' at {}, continuing to poll", status, monitorUrl)
            }

            // fails on timeout
            check(System.nanoTime() < deadline) {
                "Timeout ($timeout) while waiting for job completion at $monitorUrl"
            }

            Thread.sleep(interval.toMillis())
            interval = minOf(interval.multipliedBy(2), maxPollInterval)
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
        return asset.copy(href = absoluteHref)
    }

    /**
     * Streams the asset file into [targetDir].
     *
     * `GET asset.href` **without** `PRIVATE-TOKEN`: unlike the OGC GETs (monitor, results), the
     * href points to an object store on a different host (e.g. `object-store.os-api.cci2.ecmwf.int`, an
     * S3 back-end) that serves the resource **publicly, unauthenticated**.
     *
     * @param asset result metadata (absolute href, expected size, nullable checksum).
     * @param targetDir temporary directory to write into.
     * @return the [Path] of the downloaded asset.
     *
     * @throws IllegalStateException on a non-2xx response from the object store (it does not
     * follow RFC 7807, so only the status is reported).
     */
    private fun download(asset: RemoteAsset, targetDir: Path): Path {
        val uri = URI.create(asset.href)
        // extracts the asset name from the uri
        val fileName = uri.path.substringAfterLast('/').ifEmpty { "download" }
        val target = targetDir.resolve(fileName)

        val request = HttpRequest.newBuilder().uri(uri).GET().build() // no PRIVATE-TOKEN needed
        val response = http.send(request, HttpResponse.BodyHandlers.ofFile(target))

        // http error code check
        if (response.statusCode() !in 200..299) {
            error("Download failed (HTTP ${response.statusCode()}) from ${asset.href}")
        }
        return response.body()
    }

    /**
     * Authenticated GET (with `PRIVATE-TOKEN` header) to an OGC endpoint.
     *
     * @param url the request URL.
     * @return the [HttpResponse] received after the request.
     *
     * @throws IllegalStateException on a non-2xx response.
     */
    private fun get(url: String): HttpResponse<String> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("PRIVATE-TOKEN", token)
            .header("Accept", "application/json")
            .GET()
            .build()
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            failOnHttpError("GET $url", response)
        }
        return response
    }

    /**
     * Always throws as [IllegalStateException], enriching the message with the RFC 7807 problem-detail
     * **when** the body is interpretable.
     *
     * @param action the action performed that caused the error.
     * @param response the [HttpResponse] that caused the error.
     *
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
     * Always throws for a terminal-failure job (HTTP 200 with a `failed`/`rejected`/`dismissed` status).
     * The failure detail lives in the job's top-level `message` string (see [parseFailureMessage]),
     * **not** in an RFC 7807 body, do it is read directly; a null/blank message falls back to
     * the raw body.
     *
     * @param monitorUrl the poll url that responds with a `failed`/`rejected`/`dismissed` status.
     * @param status the failed status (`failed`/`rejected`/`dismissed`).
     * @param body the JSON body that contains the top-level `message` string.
     *
     * @throws IllegalStateException always.
     */
    private fun failOnStatus(monitorUrl: String, status: String, body: String): Nothing {
        val message = runCatching { parseFailureMessage(body) }.getOrNull()
        error(
            buildString {
                append("Job in state '$status' at $monitorUrl")
                if (message.isNullOrBlank()) append(". Body: $body") else append(": $message")
            },
        )
    }

    private companion object {
        private val logger = LoggerFactory.getLogger(CopernicusDataStoreProvider::class.java)
    }
}
