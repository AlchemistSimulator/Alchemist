/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import it.unibo.alchemist.model.geospatial.FakeHttpServer
import it.unibo.alchemist.model.geospatial.loadJsonCopernicusResponse
import java.net.http.HttpClient
import java.time.Duration
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readBytes

class TestCopernicusDataStoreProvider : StringSpec({

    val token = "test-token"

    fun createProvider(endpoint: String, timeout: Duration = Duration.ofSeconds(2)) = CopernicusDataStoreProvider(
        endpoint = endpoint,
        tokenSupplier = { token },
        http = HttpClient.newHttpClient(),
        pollInterval = Duration.ofMillis(100),
        maxPollInterval = Duration.ofMillis(300),
        timeout = timeout,
    )

    /**
     * Replaces the real hosts with the URL of the fake server.
     *
     * Failts if after the replacement, the body still refers to a real ECMWF host:
     * this is done because, without this check, a host omitted from this list would
     * not trigger a readable test error, but would instead send a read network request
     * to ECMWF servers.
     */
    fun String.withFakeBase(fake: FakeHttpServer): String {
        val replaced = replace("https://cds.climate.copernicus.eu/api", fake.baseUrl)
            .replace("https://ads.atmosphere.copernicus.eu/api", fake.baseUrl)
            .replace("https://ewds.climate.copernicus.eu/api", fake.baseUrl)
            .replace("https://object-store.os-api.cci2.ecmwf.int:443", fake.baseUrl)
        check(!replaced.contains("copernicus.eu") && !replaced.contains("ecmwf.int")) {
            "The test still reports a real ECMWF host after the replacement"
        }
        return replaced
    }

    // a simulation of HTTP requests and responses in which the entire OGC conversation is successful
    "complete the OGC workflow with actual CDS responses and downloads the asset" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()
            val payload = "test".toByteArray()

            // 1. submits the job request
            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex ->
                FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex)
            }

            // 2. polling; status: accepted
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("accepted-status.json").withFakeBase(fake))(ex)
            }

            // 3. polling; status: successful
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("successful-status.json").withFakeBase(fake))(ex)
            }

            // 4. results; adjusts the size and checksum to match the fake payload
            val resultsBody = loadBody("results.json").withFakeBase(fake)
                .replace("\"file:size\": 2331970", "\"file:size\": ${payload.size}")
                .replace(
                    "\"file:checksum\": \"b7b990dc67d490e0360c41b47fc616a6\"",
                    "\"file:checksum\": null",
                )
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results") { ex ->
                FakeHttpServer.json(200, resultsBody)(ex)
            }

            // 5. download (no token needed)
            fake.constant("GET", "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc") { ex ->
                FakeHttpServer.bytes(200, payload)(ex)
            }

            // SIMULATES THE ASSET FETCH
            provider.fetch(
                CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf("day" to "01")),
                tempDir,
            )

            val downloaded = tempDir.resolve("55f861b61cf925b229030a1faf838e93.nc")
            downloaded.shouldExist()
            downloaded.readBytes() shouldBe payload

            // the download must NOT have the token
            val downloadReq = fake.requests.find {
                it.route == "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc"
            }!!
            downloadReq.header("PRIVATE-TOKEN") shouldBe null

            // all the others requests MUST have the token
            fake.requests.filter { it !== downloadReq }.forEach {
                it.header("PRIVATE-TOKEN") shouldBe token
            }
        }
    }

    "normalizes an endpoint with a trailing slash (no '//' in the costructed URI)" {
        FakeHttpServer().use { fake ->
            // the final ‘/’ is intentional
            val provider = createProvider("${fake.baseUrl}/")
            val tempDir = createTempDirectory()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex -> FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex) }
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("successful-status.json").withFakeBase(fake))(ex)
            }
            val resultsBody = loadBody("results.json").withFakeBase(fake)
                .replace("\"file:size\": 2331970", "\"file:size\": 0")
                .replace(
                    "\"file:checksum\": \"b7b990dc67d490e0360c41b47fc616a6\"",
                    "\"file:checksum\": null",
                )
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results") { ex ->
                FakeHttpServer.json(200, resultsBody)(ex)
            }
            fake.constant("GET", "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc") { ex ->
                FakeHttpServer.bytes(200, ByteArray(0))(ex)
            }

            /*
             * If the trim function doesn't work, the constructed path would contain a double
             * slash and the FakeHttpServer would return a 404 (because no handler is registered
             * for that path) and the fetch would fail with an exception.
             */
            provider.fetch(
                CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf()),
                tempDir,
            )
            tempDir.resolve("55f861b61cf925b229030a1faf838e93.nc").shouldExist()
        }
    }

    // HTTP errors on job submit
    "returns a detailed 400 error upon submission (RFC 7807)" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue("POST", "/retrieve/v1/processes/cems-glofas-forecast/execution") { ex ->
                FakeHttpServer.json(400, loadBody("error-400-invalid-request.json").withFakeBase(fake))(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(CopernicusRequest("cems-glofas-forecast", mapOf()), tempDir)
            }
            ex.message shouldContain "400"
            ex.message shouldContain "Request has not produced a valid combination of values"
        }
    }

    "fall back to the raw body if 422 has 'detail' as an array (this is not RFC 7807)" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            val validation422Body = """
                {
                  "detail": [
                    { "loc": ["body", "inputs", "hyear"], "msg": "field required", "type": "value_error.missing" }
                  ]
                }
            """.trimIndent()

            fake.enqueue("POST", "/retrieve/v1/processes/cems-glofas-forecast/execution") { ex ->
                FakeHttpServer.json(422, validation422Body)(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(CopernicusRequest("cems-glofas-forecast", mapOf()), tempDir)
            }

            ex.message shouldContain "422"
            ex.message shouldContain "field required"
        }
    }

    // error during polling
    "returns a 401 error during polling" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue("POST", "/retrieve/v1/processes/cams-global-greenhouse-gas-forecasts/execution") { ex ->
                FakeHttpServer.json(201, loadBody("ads-submit.json").withFakeBase(fake))(ex)
            }
            fake.enqueue("GET", "/retrieve/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68") { ex ->
                FakeHttpServer.json(401, loadBody("error-401-permission-denied.json").withFakeBase(fake))(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(CopernicusRequest("cams-global-greenhouse-gas-forecasts", mapOf()), tempDir)
            }

            ex.message shouldContain "401"
            ex.message shouldContain "authentication required"
        }
    }

    listOf(
        "failed" to "Internal processing error: out of memory",
        "rejected" to "Job rejected: malformed processing chain",
        "dismissed" to "Job dismissed by the user",
    ).forEach { (status, message) ->
        "lancia eccezione per stato terminale '$status'" {
            FakeHttpServer().use { fake ->
                val provider = createProvider(fake.baseUrl)
                val tempDir = createTempDirectory()

                fake.enqueue("POST", "/retrieve/v1/processes/cems-fire-historical-v1/execution") { ex ->
                    FakeHttpServer.json(201, loadBody("ewds-submit.json").withFakeBase(fake))(ex)
                }
                fake.enqueue("GET", "/retrieve/v1/jobs/ca0c3ecc-9c02-48ad-b781-73ee0510e653") { ex ->
                    FakeHttpServer.json(
                        200,
                        """
                        {
                          "processID": "cems-fire-historical-v1",
                          "jobID": "ca0c3ecc-9c02-48ad-b781-73ee0510e653",
                          "status": "$status",
                          "message": "$message"
                        }
                        """.trimIndent(),
                    )(ex)
                }

                val ex = shouldThrow<IllegalStateException> {
                    provider.fetch(CopernicusRequest("cems-fire-historical-v1", mapOf()), tempDir)
                }
                ex.message shouldContain status
                ex.message shouldContain message
            }
        }
    }

    "continue polling for a status that has not yet been documented, instead of failing" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex -> FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex) }

            // a new status must not be treated as blocking.
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, """{ "status": "queued_for_retry" }""")(ex)
            }
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("successful-status.json").withFakeBase(fake))(ex)
            }
            val resultsBody = loadBody("results.json").withFakeBase(fake)
                .replace("\"file:size\": 2331970", "\"file:size\": 0")
                .replace(
                    "\"file:checksum\": \"b7b990dc67d490e0360c41b47fc616a6\"",
                    "\"file:checksum\": null",
                )
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results") { ex ->
                FakeHttpServer.json(200, resultsBody)(ex)
            }
            fake.constant("GET", "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc") { ex ->
                FakeHttpServer.bytes(200, ByteArray(0))(ex)
            }

            provider.fetch(
                CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf()),
                tempDir,
            )
        }
    }

    "it times out if the job does not complete" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl, timeout = Duration.ofMillis(500))
            val tempDir = createTempDirectory()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex ->
                FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex)
            }

            fake.constant("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, """{ "status": "running" }""")(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf()), tempDir)
            }
            ex.message shouldContain "Timeout"
        }
    }

    // errors on results
    "returns an error if the results return a 404 (result not ready)" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue("POST", "/retrieve/v1/processes/cams-global-greenhouse-gas-forecasts/execution") { ex ->
                FakeHttpServer.json(201, loadBody("ads-submit.json").withFakeBase(fake))(ex)
            }

            fake.enqueue("GET", "/retrieve/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68") { ex ->
                val successfulBody = """
                    {
                      "status": "successful",
                      "links": [
                        {
                          "href": "${fake.baseUrl}/retrieve/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68/results",
                          "rel": "results"
                        }
                      ]
                    }
                """.trimIndent()
                FakeHttpServer.json(200, successfulBody)(ex)
            }

            fake.enqueue("GET", "/retrieve/v1/jobs/61ebb7be-650e-4aa5-9039-6030eb01bb68/results") { ex ->
                FakeHttpServer.json(404, loadBody("error-404-result-not-ready.json").withFakeBase(fake))(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(CopernicusRequest("cams-global-greenhouse-gas-forecasts", mapOf()), tempDir)
            }
            ex.message shouldContain "404"
            ex.message shouldContain "result-not-ready"
        }
    }

    "fails if a 'successful' job does not include a rel='results' link (inconsistent server response)" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex -> FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex) }

            // 'successful' without a 'results' link
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, """{ "status": "successful", "links": [] }""")(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(
                    CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf()),
                    tempDir,
                )
            }
            ex.message shouldContain "rel='results'"
        }
    }

    // download integrity test
    "it fails with a clear error message if the downloaded size does not match" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()
            val payload = "test".toByteArray()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex -> FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex) }
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("successful-status.json").withFakeBase(fake))(ex)
            }

            // the expected size is different from that of the downloaded payload
            val resultsBody = loadBody("results.json").withFakeBase(fake)
                .replace("\"file:size\": 2331970", "\"file:size\": ${payload.size + 1}")
                .replace(
                    "\"file:checksum\": \"b7b990dc67d490e0360c41b47fc616a6\"",
                    "\"file:checksum\": null",
                )
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results") { ex ->
                FakeHttpServer.json(200, resultsBody)(ex)
            }
            fake.constant("GET", "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc") { ex ->
                FakeHttpServer.bytes(200, payload)(ex)
            }

            val ex = shouldThrow<IllegalStateException> {
                provider.fetch(
                    CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf()),
                    tempDir,
                )
            }
            ex.message shouldContain "Size mismatch"
        }
    }

    // submit request body
    "sends the serialized inputs in the submit body" {
        FakeHttpServer().use { fake ->
            val provider = createProvider(fake.baseUrl)
            val tempDir = createTempDirectory()

            fake.enqueue(
                "POST",
                "/retrieve/v1/processes/derived-era5-pressure-levels-daily-statistics/execution",
            ) { ex ->
                FakeHttpServer.json(201, loadBody("cds-submit.json").withFakeBase(fake))(ex)
            }

            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32") { ex ->
                FakeHttpServer.json(200, loadBody("successful-status.json").withFakeBase(fake))(ex)
            }

            val payload = ByteArray(0)
            val resultsBody = loadBody("results.json").withFakeBase(fake)
                .replace("\"file:size\": 2331970", "\"file:size\": 0")
                .replace(
                    "\"file:checksum\": \"b7b990dc67d490e0360c41b47fc616a6\"",
                    "\"file:checksum\": null",
                )
            fake.enqueue("GET", "/retrieve/v1/jobs/98644c83-07f4-44ff-bc6b-2969c0342a32/results") { ex ->
                FakeHttpServer.json(200, resultsBody)(ex)
            }
            fake.constant("GET", "/cci2-prod-cache-1/2026-06-28/55f861b61cf925b229030a1faf838e93.nc") { ex ->
                FakeHttpServer.bytes(200, payload)(ex)
            }

            provider.fetch(
                CopernicusRequest("derived-era5-pressure-levels-daily-statistics", mapOf("year" to "2023")),
                tempDir,
            )

            val submit = fake.requests.find { it.method == "POST" }!!
            submit.body shouldContain "\"inputs\""
            submit.body shouldContain "\"year\""
            submit.body shouldContain "2023"
        }
    }
})

private fun loadBody(fileName: String): String = loadJsonCopernicusResponse(
    fileName,
    TestCopernicusDataStoreProvider::class.java,
)
