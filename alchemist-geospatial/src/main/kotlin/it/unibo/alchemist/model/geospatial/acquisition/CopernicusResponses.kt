/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import com.google.gson.JsonParser

/*
 * Pure parsers for the JSON bodies of the ECMWF data store REST API.
 *
 * The job envelope (submit/status/results and its `links`) is an instance of "OGC API - Processes,
 * Part 1: Core (OGC 18-062r2)"; the asset metadata field names (`file:size`, `file:checksum`) are
 * from the STAC File Info Extension. ECMWF conforms to neither fully: it deviates locally (e.g. an
 * `asset.value` envelope that is not a STAC document, a bare MD5 instead of a multihash), so these
 * parsers extract only the few fields needed and tolerate the rest.
 *
 * Each function takes a raw response body string and is fully testable offline against captured
 * real responses.
 *
 * For refence:
 * Open Geospatial Consortium API: https://docs.ogc.org/is/18-062r2/18-062r2.html#toc0
 * Spatio Temporal Asset Catalogs: https://github.com/stac-extensions/file
 */

/**
 * Metadata of a result file ready for download.
 *
 * @property href absolute, unauthenticated download URL.
 * @property sizeBytes expected size in bytes; must always be verified after download.
 * @property md5 expected MD5, if advertised, else `null`.
 */
internal data class RemoteAsset(val href: String, val sizeBytes: Long, val md5: String?)

/**
 * An RFC 7807 "Problem Details" error report from the data store.
 *
 * @property type a URI (or, for ECMWF, sometimes an opaque label) identifying the problem type.
 * @property title a short, human-readable summary of the problem type.
 * @property status the HTTP status code echoed in the body, if present.
 * @property detail a human-readable explanation specific to this occurrence, if present.
 * @property instance a URI identifying the specific occurrence of the problem, if present.
 * @property traceId the data store's trace identifier (`trace_id`), useful when reporting an issue.
 */
internal data class ProblemDetail(
    val type: String,
    val title: String?,
    val status: Int?,
    val detail: String?,
    val instance: String?,
    val traceId: String?,
)

/**
 * Search for and returns the `href` property of the first link in the `links`
 * array within the [json] whose `rel` property is equal to [rel].
 *
 * ```json
 * An example of JSON body is:
 * { "links": [ { "rel": "self", "href": "..." },
 *              { "rel": "monitor", "href": "URL_TO_RETURN" } ] }
 * ```
 * In this example, with `rel = "monitor"` the return value would be `URL_TO_RETURN`
 *
 * @param json the JSON body as a string.
 * @param rel the `rel` property associated with the desired `href`.
 * @return the `href` associated with [rel] in the links array of [json], or `null` if not present.
 */
private fun linkHref(json: String, rel: String): String? = JsonParser.parseString(json)
    .asJsonObject.getAsJsonArray("links")
    ?.map { it.asJsonObject }
    ?.firstOrNull { it.get("rel")?.asString == rel }
    ?.get("href")?.asString

/**
 * Extracts the job-monitoring URL from a submit response [json] (`POST .../execute`).
 *
 * Returns the absolute `href` of the link whose `rel` is `"monitor"`.
 * The same job is also identifiable via the top-level `jobID` field and the `Location` header.
 * The `monitor` link is preferred because it keeps polling decoupled from
 * the URL path layout while remaining a pure JSON parser.
 *
 * @param json the JSON string to parse.
 * @return the `href` associated with `rel="monitor"` in the links array of [json].
 *
 * @throws IllegalStateException if no `rel="monitor"` link is present in [json].
 */
internal fun parseMonitorUrl(json: String): String =
    linkHref(json, "monitor") ?: error("No link with rel='monitor' in the submit response")

/**
 * Extracts the job status from a status (`GET .../jobs/{id}`) response [json].
 *
 * Known values include `accepted`, `running`, `successful`, `failed`, `dismissed`. Callers should
 * treat any value other than `successful`/`failed` as "keep polling", rather than enumerating the
 * intermediate states, so an unforeseen status does not break the wait loop.
 *
 * @param json the JSON string to parse.
 * @return the request's processing status.
 *
 * @throws IllegalStateException if the `status` field is absent.
 */
internal fun parseStatus(json: String): String = JsonParser.parseString(json)
    .asJsonObject.get("status")
    ?.asString
    ?: error("No 'status' field in the status response")

/**
 * Extracts the results URL from a status (`GET .../jobs/{id}`) response [json], or `null` if the
 * job exposes no results link yet.
 *
 * The `rel="results"` link appears only once the status is
 * `successful`; an `accepted`/`running` job exposes only `rel="self"`. When this returns `null`
 * on an already-successful job, the caller may fall back to `"{statusUrl}/results"`.
 *
 * @param json the JSON string to parse.
 * @return the results URL or `null` if no result link is present.
 */
internal fun parseResultsUrl(json: String): String? = linkHref(json, "results")

/**
 * Extracts the downloadable asset metadata from a results (`GET .../jobs/{id}/results`) [json].
 *
 * Reads `asset.value.href` (the download URL, served by the object store on a different host and
 * without authentication; absolute as served by ECMWF), `file:size`, and the optional
 * `file:checksum`.
 *
 * **On the shape.** The field names `file:size`/`file:checksum` are STAC File Info Extension
 * naming, but the `asset.value` envelope is an ECMWF convention, NOT STAC structure: a canonical
 * STAC asset lives in an `assets` map with these fields directly on it (no `value` wrapper), and
 * this body is not a STAC document. Hence, the two nested lookups (`asset` then `value`).
 *
 * **On the checksum.** ECMWF emits `file:checksum` as a bare lowercase MD5 hex string, despite the
 * STAC file extension nominally prescribing a self-identifying multihash, so it is captured
 * verbatim. It is nullable because some datasets/stores may omit it.
 *
 * @param json the JSON string to parse.
 * @return asset metadata as a [RemoteAsset].
 *
 * @throws IllegalStateException if `asset.value`, its `href`, or its `file:size` is absent.
 */
internal fun parseAsset(json: String): RemoteAsset {
    val value = JsonParser.parseString(json).asJsonObject
        .getAsJsonObject("asset")
        ?.getAsJsonObject("value")
        ?: error("No 'asset.value' object in the results response")
    return RemoteAsset(
        href = value.get("href")?.asString ?: error("No 'href' in asset.value"),
        sizeBytes = value.get("file:size")?.asLong ?: error("No 'file:size' in asset.value"),
        md5 = value.get("file:checksum")?.asString,
    )
}

/**
 * Parses an RFC 7807 "Problem Details" error body [json], as returned by the data store on a
 * failed request (e.g. a `404` result-not-ready, a `401` authentication required, a `403`
 * dataset-license not accepted, a `400` invalid request).
 *
 * All fields are optional per RFC 7807 except `type` (which defaults to `"about:blank"`), so every
 * field but [ProblemDetail.type] is nullable. Note that ECMWF does not always honor the spec's
 * recommendation that `type` be a URI (it sometimes repeats the human-readable title, e.g.
 * `"permission denied"`), so `type` is treated as an opaque string, never parsed as a URI.
 *
 * @param json the JSON error body as a string.
 * @return the extracted [ProblemDetail].
 */
internal fun parseProblemDetail(json: String): ProblemDetail {
    val obj = JsonParser.parseString(json).asJsonObject

    // a field extractor by name
    fun field(name: String): String? = obj.get(name)?.takeUnless { it.isJsonNull }?.asString

    return ProblemDetail(
        type = field("type") ?: "about:blank",
        title = field("title"),
        status = obj.get("status")?.takeUnless { it.isJsonNull }?.asInt,
        detail = field("detail"),
        instance = field("instance"),
        traceId = field("trace_id"),
    )
}
