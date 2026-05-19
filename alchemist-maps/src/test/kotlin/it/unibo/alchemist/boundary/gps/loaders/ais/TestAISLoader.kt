/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.gps.loaders.AISLoader
import it.unibo.alchemist.boundary.gps.loaders.toTraces
import java.time.Instant

class TestAISLoader :
    StringSpec({
        "AIS payloads should be converted to epoch-based trace times by default" {
            val trace = listOf(
                payloadAt(10, vesselId = 1),
                payloadAt(12, vesselId = 1),
            ).toTraces().single()
            trace.startTime.toDouble() shouldBe 10.0
            trace.finalTime.toDouble() shouldBe 12.0
        }

        "AIS payloads from the same vessel should be ordered by timestamp" {
            val trace = listOf(
                payloadAt(12, vesselId = 1),
                payloadAt(10, vesselId = 1),
            ).toTraces().single()
            trace.initialPosition.time.toDouble() shouldBe 10.0
            trace.finalPosition.time.toDouble() shouldBe 12.0
        }

        "AIS payloads should support realignment to a custom time origin" {
            val origin = Instant.parse("2026-05-15T12:00:00Z")
            val traces = listOf(
                payloadAt(origin, 8, vesselId = 2),
                payloadAt(origin, 5, vesselId = 1),
                payloadAt(origin, 3, vesselId = 2),
            ).toTraces(timeOrigin = origin)
            traces
                .map { it.startTime.toDouble() to it.finalTime.toDouble() }
                .toSet() shouldBe setOf(5.0 to 5.0, 3.0 to 8.0)
        }

        "AISLoader should advertise AIS-like file extensions" {
            val extensions = AISLoader().supportedExtensions()
            extensions shouldContain "ais"
            extensions shouldContain "nmea"
            extensions shouldContain "txt"
        }
    })

private fun payloadAt(secondsFromEpoch: Long, vesselId: Int): AISPayload =
    payloadAt(Instant.EPOCH, secondsFromEpoch, vesselId)

private fun payloadAt(origin: Instant, secondsFromOrigin: Long, vesselId: Int): AISPayload = AISPayload(
    vesselId = vesselId,
    timestamp = origin.plusSeconds(secondsFromOrigin),
    longitude = vesselId.toDouble(),
    latitude = vesselId.toDouble(),
)
