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

/*
 * Parsers of the JSON bodies of the API of the Copernicus data stores, which follows OGC API - Processes
 * (https://docs.ogc.org/is/18-062r2/18-062r2.html) loosely: they read only the fields needed, and ignore the rest.
 */

/**
 * A file to download from [href], with its size in bytes and, if advertised, its MD5 digest
 * as the data store wrote it (see [parseAsset]).
 */
internal data class RemoteAsset(val href: String, val sizeBytes: Long, val md5: String?)

/**
 * An [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) error report of the data store, with its trace identifier
 * and, for a failed job, the [traceback] of the backend, often the only field with the actual cause.
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

/**
 * The `href` of the first entry in the `links` of [json] with the given [rel], if any.
 */
private fun linkHref(json: String, rel: String): String? = JsonParser.parseString(json)
    .asJsonObject.getAsJsonArray("links")
    ?.map { it.asJsonObject }
    ?.firstOrNull { it.get("rel")?.asString == rel }
    ?.get("href")?.asString

/**
 * The URL to monitor the job, from the response [json] to its submission.
 *
 * @throws IllegalStateException if there is none.
 */
internal fun parseMonitorUrl(json: String): String =
    linkHref(json, "monitor") ?: error("No link with rel='monitor' in the submit response")

/**
 * The status of the job in the status response [json], as a string: the data store may add new ones.
 *
 * @throws IllegalStateException if there is none.
 */
internal fun parseStatus(json: String): String = JsonParser.parseString(json)
    .asJsonObject.get("status")
    ?.asString
    ?: error("No 'status' field in the status response")

/**
 * The URL of the results of the job, from the status response [json]; `null` until the job ends.
 * For a failed job, it answers with an error report (see [parseProblemDetail]) explaining the failure.
 */
internal fun parseResultsUrl(json: String): String? = linkHref(json, "results")

/**
 * The file to download, from the results response [json].
 * The MD5 digest is kept as written, without the leading zeros the data store drops.
 *
 * @throws IllegalStateException if the URL or the size of the file is missing.
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
 * The error report in [json]. Its `type` is not always a URI, so it is kept as a plain string.
 */
internal fun parseProblemDetail(json: String): ProblemDetail {
    val obj = JsonParser.parseString(json).asJsonObject

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
 * The `message` of the status response [json], if any.
 * The data stores currently leave it empty, and explain failures in the results instead (see [parseResultsUrl]).
 */
internal fun parseFailureMessage(json: String): String? = JsonParser.parseString(json)
    .asJsonObject.get("message")?.takeUnless { it.isJsonNull }?.asString
