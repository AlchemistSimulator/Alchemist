/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldStartWith
import it.unibo.alchemist.model.geospatial.acquisition.utility.ProblemDetail
import it.unibo.alchemist.model.geospatial.acquisition.utility.RemoteAsset
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseAsset
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseFailureMessage
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseMonitorUrl
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseProblemDetail
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseResultsUrl
import it.unibo.alchemist.model.geospatial.acquisition.utility.parseStatus
import it.unibo.alchemist.model.geospatial.loadJsonCopernicusResponse

/**
 * The test data are ACTUAL response bodies, captured from the three ECMWF data stores (CDS / ADS / EWDS)
 * against the current `/execution` submit path. Each store contributes one full job lifecycle,
 * all four bodies sharing the same job id: submit -> accepted -> successful -> results.
 * Error bodies were captured separately.
 */
class TestCopernicusParsers : StringSpec({

    /**
     * One full job lifecycle captured from a single data store.
     */
    data class StoreCase(
        val store: String,
        val submit: String,
        val accepted: String,
        val successful: String,
        val results: String,
        val monitorUrl: String,
        val resultsUrl: String,
        val asset: RemoteAsset,
    )

    val cds = StoreCase(
        store = "CDS",
        submit = loadBody("cds-submit.json"),
        accepted = loadBody("cds-accepted-status.json"),
        successful = loadBody("cds-successful-status.json"),
        results = loadBody("cds-results.json"),
        monitorUrl = "https://cds.climate.copernicus.eu/api/retrieve" +
            "/v1/jobs/82d0a5fb-f096-42fd-b644-c7ba173b0154",
        resultsUrl = "https://cds.climate.copernicus.eu/api/retrieve" +
            "/v1/jobs/82d0a5fb-f096-42fd-b644-c7ba173b0154/results",
        asset = RemoteAsset(
            href = "https://object-store.os-api.cci2.ecmwf.int:443/" +
                "cci2-prod-cache-1/2026-08-08/f8ec201f667455bd3cf338c39fc03a1a.zip",
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
        monitorUrl = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
            "/v1/jobs/a79e0cce-9c46-4bd9-aec5-3570977cbbd1",
        resultsUrl = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
            "/v1/jobs/a79e0cce-9c46-4bd9-aec5-3570977cbbd1/results",
        asset = RemoteAsset(
            href = "https://object-store.os-api.cci2.ecmwf.int:443/" +
                "cci2-prod-cache-2/2026-08-08/1b2c8f7e437451ffc09a9e23cb32a542.zip",
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
        monitorUrl = "https://ewds.climate.copernicus.eu/api/retrieve" +
            "/v1/jobs/bb1ee550-0dea-4c84-b164-b8c54165a25f",
        resultsUrl = "https://ewds.climate.copernicus.eu/api/retrieve" +
            "/v1/jobs/bb1ee550-0dea-4c84-b164-b8c54165a25f/results",
        asset = RemoteAsset(
            href = "https://object-store.os-api.cci2.ecmwf.int:443/" +
                "cci2-prod-cache-3/2026-08-09/9600fbec69609809250b901b42f6800.zip",
            sizeBytes = 22_643L,
            md5 = "104b13e69dc13b15f75c910d08f0e4ac",
        ),
    )

    val stores = listOf(cds, ads, ewds)

    /**
     * Runs [check] on every store, reporting which one failed.
     */
    fun eachStore(check: (StoreCase) -> Unit) = stores.forEach { case ->
        withClue("store: ${case.store}") { check(case) }
    }

    // link extraction
    "parseMonitorUrl extracts the monitor link from a submit response" {
        eachStore { parseMonitorUrl(it.submit) shouldBe it.monitorUrl }
    }

    // status extraction
    "parseStatus reads 'accepted' from a pending job" {
        eachStore { parseStatus(it.accepted) shouldBe "accepted" }
    }

    "parseStatus reads 'successful' from a finished job" {
        eachStore { parseStatus(it.successful) shouldBe "successful" }
    }

    // results link extraction
    "parseResultsUrl returns null while the job is not yet ready" {
        eachStore { parseResultsUrl(it.accepted) shouldBe null }
    }

    "parseResultsUrl returns the results link once the job is successful" {
        eachStore { parseResultsUrl(it.successful) shouldBe it.resultsUrl }
    }

    // asset metadata extraction
    "parseAsset extracts href, size and checksum from a results response" {
        eachStore { parseAsset(it.results) shouldBe it.asset }
    }

    // error bodies: RFC 7807
    "parseProblemDetail extracts all fields from a 404 result-not-ready body" {
        parseProblemDetail(loadBody("error-404-result-not-ready.json")) shouldBe ProblemDetail(
            type = "http://www.opengis.net/def/exceptions/ogcapi-processes-1/1.0/result-not-ready",
            title = "job results not ready",
            status = 404,
            detail = "status of 61ebb7be-650e-4aa5-9039-6030eb01bb68 is 'accepted'",
            instance = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
                "/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68/results",
            traceId = "e7ba3606-9816-43cc-ab6a-4f0642388701",
            traceback = null,
        )
    }

    "parseProblemDetail handles a 401 whose type is a string label, not a URI" {
        parseProblemDetail(loadBody("error-401-permission-denied.json")) shouldBe ProblemDetail(
            type = "permission denied",
            title = "permission denied",
            status = 401,
            detail = "authentication required",
            instance = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
                "/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68",
            traceId = "b63a2882-2510-4ced-935a-b2faec13eead",
            traceback = null,
        )
    }

    "parseProblemDetail defaults type to 'about:blank' and nulls absent fields" {
        parseProblemDetail("""{"detail":"something went wrong"}""") shouldBe ProblemDetail(
            type = "about:blank",
            title = null,
            status = null,
            detail = "something went wrong",
            instance = null,
            traceId = null,
            traceback = null,
        )
    }

    "parseProblemDetail extracts the traceback from a failed job's results body" {
        val problem = parseProblemDetail(loadBody("ewds-failed-results.json"))
        problem.type shouldBe "job results failed"
        problem.title shouldBe "The job has failed"
        problem.status shouldBe 400
        problem.detail shouldBe null
        problem.instance shouldBe "https://ewds.climate.copernicus.eu/api/retrieve" +
            "/v1/jobs/3a891e6b-e602-400d-9f26-d9be8acadc05/results"
        problem.traceId shouldBe "4e257fcd-dda1-494a-8e26-d5d6885676d4"
        problem.traceback shouldStartWith "The job failed with: MultiAdaptorNoDataError"
    }

    "describe falls back to the traceback when the body carries no detail" {
        val described = parseProblemDetail(loadBody("ewds-failed-results.json")).describe()
        described shouldContain "MultiAdaptorNoDataError"
        described shouldContain "type=job results failed"
        described shouldContain "trace=4e257fcd-dda1-494a-8e26-d5d6885676d4"
    }

    // synthetic: no real body was captured that populates the OGC 'message' field.
    "parseFailureMessage reads a message string" {
        parseFailureMessage("""{"status":"failed","message":"the job blew up"}""") shouldBe "the job blew up"
    }

    "parseFailureMessage returns null when no message is present" {
        parseFailureMessage("""{"status":"failed"}""") shouldBe null
    }

    "parseFailureMessage returns null when message is JSON null" {
        parseFailureMessage("""{"status":"failed","message":null}""") shouldBe null
    }
})

private fun loadBody(fileName: String): String = loadJsonCopernicusResponse(
    fileName,
    TestCopernicusParsers::class.java,
)
