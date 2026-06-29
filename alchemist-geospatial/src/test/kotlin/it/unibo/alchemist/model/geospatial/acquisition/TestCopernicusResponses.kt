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

/**
 * The test data, the ACTUAL response bodies, were captured on 2026-06-28 (yyyy-mm-dd)
 * from several ECMWF stores (CDS / EWDS / ADS). The EWDS and ADS submit bodies are verbatim;
 * the CDS submit body is truncated. The expected values are read manually from each body.
 */
class TestCopernicusResponses : StringSpec({

    // a captured submit response paired with the monitor URL expected from it.
    data class SubmitCase(val store: String, val body: String, val expectedMonitorUrl: String)

    // some ACTUAL responses received after submit requests
    val submitCases = listOf(
        SubmitCase(
            store = "CDS",
            body = loadBody("cds-submit.json"),
            expectedMonitorUrl = "https://cds.climate.copernicus.eu/api/retrieve" +
                "/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32",
        ),
        SubmitCase(
            store = "EWDS",
            body = loadBody("ewds-submit.json"),
            expectedMonitorUrl = "https://ewds.climate.copernicus.eu/api/retrieve" +
                "/v1/jobs/ca0c3ecc-9c02-48ad-b781-73ee0510e653",
        ),
        SubmitCase(
            store = "ADS",
            body = loadBody("ads-submit.json"),
            expectedMonitorUrl = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
                "/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68",
        ),
    )

    // status bodies. One successful (adds a results link), one accepted (only a self link)
    val successfulStatus = loadBody("successful-status.json")
    val acceptedStatus = loadBody("accepted-status.json")

    // results body (from a GET .../jobs/{id}/results request)
    val resultsBody = loadBody("results.json")

    // link extraction
    "parseMonitorUrl extracts the monitor link from a submit response" {
        submitCases.forEach { case ->
            withClue("store: ${case.store}") {
                parseMonitorUrl(case.body) shouldBe case.expectedMonitorUrl
            }
        }
    }

    // status extraction
    "parseStatus reads 'successful' from a finished job" {
        parseStatus(successfulStatus) shouldBe "successful"
    }

    "parseStatus reads 'accepted' from a pending job" {
        parseStatus(acceptedStatus) shouldBe "accepted"
    }

    // download link extraction
    "parseResultsUrl returns the results link once the job is successful" {
        parseResultsUrl(successfulStatus) shouldBe
            "https://cds.climate.copernicus.eu/api/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results"
    }

    "parseResultsUrl returns null while the job is not yet ready" {
        parseResultsUrl(acceptedStatus) shouldBe null
    }

    // asset metadata extraction
    "parseAsset extracts href, size and checksum from a results response" {
        parseAsset(resultsBody) shouldBe RemoteAsset(
            href = "https://object-store.os-api.cci2.ecmwf.int:443/" +
                "cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc",
            sizeBytes = 2_331_970L,
            md5 = "b7b990dc67d490e0360c41b47fc616a6",
        )
    }

    // errors extraction
    "parseProblemDetail extracts all fields from a 404 result-not-ready body" {
        parseProblemDetail(loadBody("error-404-result-not-ready.json")) shouldBe ProblemDetail(
            type = "http://www.opengis.net/def/exceptions/ogcapi-processes-1/1.0/result-not-ready",
            title = "job results not ready",
            status = 404,
            detail = "status of 61ebb7be-650e-4aa5-9039-6030eb01bb68 is 'accepted'",
            instance = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
                "/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68/results",
            traceId = "e7ba3606-9816-43cc-ab6a-4f0642388701",
        )
    }

    "parseProblemDetail handles a 401 whose type is a free-string label, not a URI" {
        parseProblemDetail(loadBody("error-401-permission-denied.json")) shouldBe ProblemDetail(
            type = "permission denied",
            title = "permission denied",
            status = 401,
            detail = "authentication required",
            instance = "https://ads.atmosphere.copernicus.eu/api/retrieve" +
                "/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68",
            traceId = "b63a2882-2510-4ced-935a-b2faec13eead",
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
        )
    }

    "parseProblemDetail extracts all fields from a 400 invalid-request body" {
        parseProblemDetail(loadBody("error-400-invalid-request.json")) shouldBe ProblemDetail(
            type = "invalid request",
            title = "invalid request",
            status = 400,
            detail = "Request has not produced a valid combination of values, " +
                "please check your selection.\n" +
                "{'data_format': ['netcdf'], 'day': ['10'], 'hydrological_model': ['lisflood'], " +
                "'leadtime_hour': ['26'], 'month': ['02'], 'product_type': ['control_forecast'], " +
                "'system_version': ['operational'], 'variable': ['river_discharge_in_the_last_24_hours'], " +
                "'year': ['2024']}",
            instance = "https://ewds.climate.copernicus.eu/api/retrieve" +
                "/v1/processes/cems-glofas-forecast/execute",
            traceId = "cec329b8-cb55-4b84-a3a8-86b85facdbb4",
        )
    }

    /*
     * no real failed-job body has been captured (failed jobs are rare on stable datasets).
     * A failed job would occur after a successful submit (all fields in the request are validated
     * correctly in the ECMWF backend) but somehow the requested data can't be processed/returned.
     * The following are SYNTHETIC bodies, NOT ECMWF's failed-jobs responses.
     */
    "parseFailureMessage reads a top-level message string" {
        parseFailureMessage("""{"status":"failed","message":"the job blew up"}""") shouldBe "the job blew up"
    }

    "parseFailureMessage returns null when no message is present" {
        parseFailureMessage("""{"status":"failed"}""") shouldBe null
    }

    "parseFailureMessage returns null when message is JSON null" {
        parseFailureMessage("""{"status":"failed","message":null}""") shouldBe null
    }
})

/**
 * Loads the JSON bodies from the .../resources/copernicus-responses/ directory.
 *
 * @param fileName the name of the JSON file to load.
 * @return the JSON body of [fileName] as a string.
 *
 * @throws IllegalStateException if no file with name [fileName] is found.
 */
private fun loadBody(fileName: String): String = checkNotNull(
    TestCopernicusResponses::class.java.getResourceAsStream("/copernicus-responses/$fileName"),
) {
    "Missing test fixture: $fileName"
}.bufferedReader().use { it.readText() }
