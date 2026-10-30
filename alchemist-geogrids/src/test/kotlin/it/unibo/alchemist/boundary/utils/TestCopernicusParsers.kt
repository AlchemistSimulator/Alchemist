/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import it.unibo.alchemist.loadJsonCopernicusResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The test data are ACTUAL response bodies, captured from the three ECMWF data stores (CDS / ADS / EWDS)
 * against the current `/execution` submit path. Each store contributes one full job lifecycle,
 * all four bodies sharing the same job id: submit -> accepted -> successful -> results.
 * Error bodies were captured separately.
 */
class TestCopernicusParsers {

    /**
     * Runs [check] on every store, passing a message reporting which one is being checked.
     */
    private fun eachStore(check: (StoreCase, String) -> Unit) = stores.forEach { case ->
        check(case, "store: ${case.store}")
    }

    // link extraction
    @Test
    fun `parseMonitorUrl extracts the monitor link from a submit response`() {
        eachStore { case, clue -> assertEquals(case.monitorUrl, parseMonitorUrl(case.submit), clue) }
    }

    // status extraction
    @Test
    fun `parseStatus reads 'accepted' from a pending job`() {
        eachStore { case, clue -> assertEquals("accepted", parseStatus(case.accepted), clue) }
    }

    @Test
    fun `parseStatus reads 'successful' from a finished job`() {
        eachStore { case, clue -> assertEquals("successful", parseStatus(case.successful), clue) }
    }

    // results link extraction
    @Test
    fun `parseResultsUrl returns null while the job is not yet ready`() {
        eachStore { case, clue -> assertNull(parseResultsUrl(case.accepted), clue) }
    }

    @Test
    fun `parseResultsUrl returns the results link once the job is successful`() {
        eachStore { case, clue -> assertEquals(case.resultsUrl, parseResultsUrl(case.successful), clue) }
    }

    // asset metadata extraction
    @Test
    fun `parseAsset extracts href, size and checksum from a results response`() {
        eachStore { case, clue -> assertEquals(case.asset, parseAsset(case.results), clue) }
    }

    // error bodies: RFC 7807
    @Test
    fun `parseProblemDetail extracts all fields from a 404 result-not-ready body`() {
        assertEquals(
            ProblemDetail(
                type = "http://www.opengis.net/def/exceptions/ogcapi-processes-1/1.0/result-not-ready",
                title = "job results not ready",
                status = 404,
                detail = "status of 61ebb7be-650e-4aa5-9039-6030eb01bb68 is 'accepted'",
                instance = "$ADS_JOBS_ENDPOINT/61ebb7be-650e-4aa5-9039-6030eb01bb68/results",
                traceId = "e7ba3606-9816-43cc-ab6a-4f0642388701",
                traceback = null,
            ),
            parseProblemDetail(loadBody("error-404-result-not-ready.json")),
        )
    }

    @Test
    fun `parseProblemDetail handles a 401 whose type is a string label, not a URI`() {
        assertEquals(
            ProblemDetail(
                type = "permission denied",
                title = "permission denied",
                status = 401,
                detail = "authentication required",
                instance = "$ADS_JOBS_ENDPOINT/61ebb7be-650e-4aa5-9039-6030eb01bb68",
                traceId = "b63a2882-2510-4ced-935a-b2faec13eead",
                traceback = null,
            ),
            parseProblemDetail(loadBody("error-401-permission-denied.json")),
        )
    }

    @Test
    fun `parseProblemDetail defaults type to about-blank and nulls absent fields`() {
        assertEquals(
            ProblemDetail(
                type = "about:blank",
                title = null,
                status = null,
                detail = "something went wrong",
                instance = null,
                traceId = null,
                traceback = null,
            ),
            parseProblemDetail("""{"detail":"something went wrong"}"""),
        )
    }

    @Test
    fun `parseProblemDetail extracts the traceback from a failed job's results body`() {
        val problem = parseProblemDetail(loadBody("ewds-failed-results.json"))
        assertEquals("job results failed", problem.type)
        assertEquals("The job has failed", problem.title)
        assertEquals(400, problem.status)
        assertNull(problem.detail)
        assertEquals("$EWDS_JOBS_ENDPOINT/3a891e6b-e602-400d-9f26-d9be8acadc05/results", problem.instance)
        assertEquals("4e257fcd-dda1-494a-8e26-d5d6885676d4", problem.traceId)
        val traceback = problem.traceback
        assertTrue(
            traceback?.startsWith("The job failed with: MultiAdaptorNoDataError") == true,
            "unexpected traceback: $traceback",
        )
    }

    @Test
    fun `describe falls back to the traceback when the body carries no detail`() {
        val described = parseProblemDetail(loadBody("ewds-failed-results.json")).describe()
        listOf(
            "MultiAdaptorNoDataError",
            "type=job results failed",
            "trace=4e257fcd-dda1-494a-8e26-d5d6885676d4",
        ).forEach { expected ->
            assertTrue(expected in described, "'$described' should contain '$expected'")
        }
    }

    // synthetic: no real body was captured that populates the OGC 'message' field.
    @Test
    fun `parseFailureMessage reads a message string`() {
        assertEquals("the job blew up", parseFailureMessage("""{"status":"failed","message":"the job blew up"}"""))
    }

    @Test
    fun `parseFailureMessage returns null when no message is present`() {
        assertNull(parseFailureMessage("""{"status":"failed"}"""))
    }

    @Test
    fun `parseFailureMessage returns null when message is JSON null`() {
        assertNull(parseFailureMessage("""{"status":"failed","message":null}"""))
    }

    /**
     * One full job lifecycle captured from a single data store.
     */
    private data class StoreCase(
        val store: String,
        val submit: String,
        val accepted: String,
        val successful: String,
        val results: String,
        val monitorUrl: String,
        val resultsUrl: String,
        val asset: RemoteAsset,
    )

    private companion object {
        const val OBJECT_STORE_ENDPOINT = "https://object-store.os-api.cci2.ecmwf.int:443"
        const val ADS_JOBS_ENDPOINT = "https://ads.atmosphere.copernicus.eu/api/retrieve/v1/jobs"
        const val EWDS_JOBS_ENDPOINT = "https://ewds.climate.copernicus.eu/api/retrieve/v1/jobs"
        const val CDS_JOBS_ENDPOINT = "https://cds.climate.copernicus.eu/api/retrieve/v1/jobs"

        val cds = StoreCase(
            store = "CDS",
            submit = loadBody("cds-submit.json"),
            accepted = loadBody("cds-accepted-status.json"),
            successful = loadBody("cds-successful-status.json"),
            results = loadBody("cds-results.json"),
            monitorUrl = "$CDS_JOBS_ENDPOINT/82d0a5fb-f096-42fd-b644-c7ba173b0154",
            resultsUrl = "$CDS_JOBS_ENDPOINT/82d0a5fb-f096-42fd-b644-c7ba173b0154/results",
            asset = RemoteAsset(
                href = "$OBJECT_STORE_ENDPOINT/cci2-prod-cache-1/2026-08-08/f8ec201f667455bd3cf338c39fc03a1a.zip",
                sizeBytes = 50_689L,
                md5 = "aa45b382ed6a3d13a4f30cca4d0a9b7",
            ),
        )

        val ads = StoreCase(
            store = "ADS",
            submit = loadBody("ads-submit.json"),
            accepted = loadBody("ads-accepted-status.json"),
            successful = loadBody("ads-successful-status.json"),
            results = loadBody("ads-results.json"),
            monitorUrl = "$ADS_JOBS_ENDPOINT/a79e0cce-9c46-4bd9-aec5-3570977cbbd1",
            resultsUrl = "$ADS_JOBS_ENDPOINT/a79e0cce-9c46-4bd9-aec5-3570977cbbd1/results",
            asset = RemoteAsset(
                href = "$OBJECT_STORE_ENDPOINT/cci2-prod-cache-2/2026-08-08/1b2c8f7e437451ffc09a9e23cb32a542.zip",
                sizeBytes = 7_768_356L,
                md5 = "d6c0964f89e3f43d1a99ee4d7722f505",
            ),
        )

        val ewds = StoreCase(
            store = "EWDS",
            submit = loadBody("ewds-submit.json"),
            accepted = loadBody("ewds-accepted-status.json"),
            successful = loadBody("ewds-successful-status.json"),
            results = loadBody("ewds-results.json"),
            monitorUrl = "$EWDS_JOBS_ENDPOINT/bb1ee550-0dea-4c84-b164-b8c54165a25f",
            resultsUrl = "$EWDS_JOBS_ENDPOINT/bb1ee550-0dea-4c84-b164-b8c54165a25f/results",
            asset = RemoteAsset(
                href = "$OBJECT_STORE_ENDPOINT/cci2-prod-cache-3/2026-08-09/9600fbec69609809250b901b42f6800.zip",
                sizeBytes = 22_643L,
                md5 = "104b13e69dc13b15f75c910d08f0e4ac",
            ),
        )

        val stores = listOf(cds, ads, ewds)
    }
}

private fun loadBody(fileName: String): String = loadJsonCopernicusResponse(
    fileName,
    TestCopernicusParsers::class.java,
)
