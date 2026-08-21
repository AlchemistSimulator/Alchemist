/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

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
 * Error bodies come in three distinct shapes, each parsed (or not) accordingly:
 * - RFC 7807 problem-details on 4xx application errors (400/401/403/404) -> see parseProblemDetail(...).
 * - FastAPI/Pydantic validation errors on 422, whose `detail` is an array of objects, NOT a
 *   string -> parseProblemDetail(...) yields a null `detail` for them.
 * - the cause of a FAILED JOB, which despite the OGC schema declaring a top-level `message` string
 *   is not served there: ECMWF leaves `message` empty and exposes the cause only by referencing
 *   the job's `rel="results"` link, which answers 4xx with a problem-details body carrying a
 *   proprietary `traceback` field -> see parseProblemDetail(...); parseFailureMessage(...) reads
 *   `message` as a fallback only.
 *
 * Each function takes a raw response body string and is fully testable offline against captured
 * real responses.
 *
 * For reference:
 * Open Geospatial Consortium API: https://docs.ogc.org/is/18-062r2/18-062r2.html#toc0
 * Spatio Temporal Asset Catalogs: https://github.com/stac-extensions/file
 */

/**
 * Metadata of a result file ready for download.
 *
 * @property href absolute, unauthenticated download URL.
 * @property sizeBytes expected size in bytes; must always be verified after download.
 * @property md5 md5 expected MD5 as advertised, verbatim, if advertised at all; it may be shorter than
 * 32 characters, see [parseAsset].
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
 * @property traceback the backend failure report of a failed job. A **proprietary ECMWF
 * extension**, not part of RFC 7807, served by the results endpoint; frequently the only field
 * carrying the actual cause, since such bodies often have no `detail`.
 */
internal data class ProblemDetail(
    val type: String,
    val title: String?,
    val status: Int?,
    val detail: String?,
    val instance: String?,
    val traceId: String?,
    val traceback: String?,
) {
    /**
     * Extracts a human-readable summary of this problem-detail, or an empty string if
     * no content is extractable. Prefers [detail] (the standard field),
     * then [traceback] (the only content of a failed-job body), then [title].
     *
     * @return a human-readable string describing this problem.
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

/**
 * Search for and returns the `href` property of the first link in the `links`
 * array within the [json] whose `rel` property is equal to [rel]. Returns `null`
 * if the body has no `links` array at all.
 *
 * ```json
 * An example of JSON body is:
 * { "links": [ { "rel": "self", "href": "..." },
 *              { "rel": "monitor", "href": "URL_TO_RETURN" } ] }
 * ```
 * In this example, with `rel = "monitor"` the return value would be `URL_TO_RETURN`.
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
 * Extracts the job-monitoring URL from a submit response [json] (`POST .../execution`).
 *
 * Returns the absolute `href` of the link whose `rel` is `"monitor"`.
 * The same job is also identifiable via the top-level `jobID` field and the `Location` response header.
 * The `monitor` link is preferred because it keeps polling decoupled from the URL path
 * layout while remaining a pure JSON parser.
 *
 * @param json the JSON string to parse.
 * @return the `href` associated with `rel="monitor"` in the links array of [json].
 * @throws IllegalStateException if no `rel="monitor"` link is present in [json].
 */
internal fun parseMonitorUrl(json: String): String =
    linkHref(json, "monitor") ?: error("No link with rel='monitor' in the submit response")

/**
 * Extracts the job status from a status (`GET .../jobs/{id}`) response [json].
 *
 * The states handled by the official ECMWF client are `accepted`, `running`, `successful`,
 * `failed`, `rejected`, `dismissed`, and `deleted`. Of these, `successful` is the sole success;
 * `failed`, `rejected`, `dismissed`, `deleted` are terminal failures; `accepted`/`running` are
 * transient.
 *
 * The status is returned as a **raw string**, not an enum, because the API is marked
 * as "evolving" and the set of states is descriptive rather than contractual: the official
 * client itself types it as a plain string and provides for an unrecognized value.
 *
 * @param json the JSON string to parse.
 * @return the job's processing status, verbatim.
 * @throws IllegalStateException if the `status` field is absent.
 */
internal fun parseStatus(json: String): String = JsonParser.parseString(json)
    .asJsonObject.get("status")
    ?.asString
    ?: error("No 'status' field in the status response")

/**
 * Extracts the results URL from a status (`GET .../jobs/{id}`) response [json], or `null` if the
 * job exposes no results link.
 *
 * The `rel="results"` link appears once the job reaches a terminal state; an `accepted`/`running`
 * job exposes only `rel="self"`. On a `successful` job the link serves the asset metadata (see
 * [parseAsset]); on a failed one it answers 4xx with the problem-details body that carries the
 * actual cause. A `null` on an already-`successful` job indicates an inconsistent server response,
 * and the caller should fail rather than reconstructing a `.../results` path by hand.
 *
 * @param json the JSON string to parse.
 * @return the results URL, or `null` if no `rel="results"` link is present.
 */
internal fun parseResultsUrl(json: String): String? = linkHref(json, "results")

/**
 * Extracts the downloadable asset metadata from a results (`GET .../jobs/{id}/results`) [json].
 *
 * Reads `asset.value.href` (the download URL, served by the object store on a different host and
 * without authentication; absolute as served by ECMWF), `file:size`, and the optional
 * `file:checksum`.
 *
 * **Note on the shape**: the field names `file:size`/`file:checksum` are STAC File Info Extension
 * naming, but the `asset.value` envelope is an ECMWF convention, NOT STAC structure: a canonical
 * STAC asset lives in an `assets` map with these fields directly on it (no `value` wrapper), and
 * this body is not a STAC document. Hence, the two nested lookups (`asset` then `value`).
 *
 * **Note on the checksum**: ECMWF emits `file:checksum` as a bare lowercase MD5 hex string.
 * It is captured verbatim and **not** normalized here, because a parser must report what
 * the server said: the store omits left zero-padding, so a digest beginning with a zero
 * is advertised with fewer than 32 characters (e.g., `0aa45b...` served as `aa45b...`).
 * Padding it back before comparison is the responsibility of the integrity check, not of this parser.
 * The field is nullable because some datasets/stores omit it entirely.
 *
 * @param json the JSON string to parse.
 * @return asset metadata as a [RemoteAsset].
 * @throws IllegalStateException if `asset.value`, its `href`, or its `file:size` is absent.
 */
internal fun parseAsset(json: String): RemoteAsset {
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
    fun field(name: String) = obj.get(name)?.takeIf { it.isJsonPrimitive }?.asJsonPrimitive
    return ProblemDetail(
        type = field("type")?.asString ?: "about:blank",
        title = field("title")?.asString,
        status = field("status")?.asInt,
        detail = field("detail")?.asString,
        instance = field("instance")?.asString,
        traceId = field("trace_id")?.asString,
        traceback = field("traceback")?.asString,
    )
}

/**
 * Extracts the OGC `message` field from a job status body, or `null` if absent.
 *
 * The OGC schema declares a nullable top-level `message` string as the place where a job reports
 * what happened, but **ECMWF does not populate it**: the cause of a failure is served instead by
 * dereferencing the job's `rel="results"` link (see [parseResultsUrl] and [parseProblemDetail]).
 * This parser is, therefore, a fallback, kept because `message` is part of the schema and a future
 * revision of the data store may start filling it in.
 *
 * It returns `null` instead of throwing on an absent or null field, both because the schema
 * declares it nullable and because it runs on the error path, where the caller falls back to the
 * raw body.
 *
 * @param json the JSON status body as a string.
 * @return the `message` string, or `null` if missing/null.
 */
internal fun parseFailureMessage(json: String): String? = JsonParser.parseString(json)
    .asJsonObject.get("message")?.takeUnless { it.isJsonNull }?.asString
