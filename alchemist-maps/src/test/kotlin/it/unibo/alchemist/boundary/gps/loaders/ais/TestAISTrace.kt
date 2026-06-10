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
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISTrace :
    StringSpec({
        "AIS traces should discard vessel streams without positions" {
            AISTrace.from(
                listOf(
                    payloadAt(1, vesselMMSI = 1, vesselName = "STATIC"),
                    payloadAt(2, vesselMMSI = 1, callsign = "CALL"),
                ),
            ) shouldBe emptyList()
        }

        "AIS traces should build progressively complete payloads" {
            val trace = AISTrace.from(
                listOf(
                    payloadAt(1, vesselName = "VESSEL", shipType = AISShipType.PilotVessel),
                    payloadAt(2, longitude = 10.0, latitude = 44.0),
                ),
            ).single()
            trace.payloads shouldContainExactly listOf(
                payloadAt(1, vesselName = "VESSEL", shipType = AISShipType.PilotVessel),
                payloadAt(
                    2,
                    longitude = 10.0,
                    latitude = 44.0,
                    vesselName = "VESSEL",
                    shipType = AISShipType.PilotVessel,
                ),
            )
        }

        "AIS traces should interpolate numeric measures and use closest non-numeric values" {
            val trace = AISTrace.from(
                listOf(
                    payloadAt(0, longitude = 0.0, latitude = 0.0, headingDegrees = 10.0),
                    payloadAt(9, shipType = AISShipType.PilotVessel),
                    payloadAt(10, longitude = 10.0, latitude = 10.0, headingDegrees = 20.0),
                ),
            ).single()
            val payload = trace.payloadAt(DoubleTime(9.0))
            payload.headingDegrees shouldBe (19.0 plusOrMinus 1e-15)
            payload.longitude shouldBe (9.0 plusOrMinus 1e-15)
            payload.latitude shouldBe (9.0 plusOrMinus 1e-15)
            payload.shipType shouldBe AISShipType.PilotVessel
        }

        "AIS traces should compare by MMSI" {
            AISTrace.from(
                listOf(
                    payloadAt(1, vesselMMSI = 2, longitude = 0.0, latitude = 0.0),
                    payloadAt(1, vesselMMSI = 1, longitude = 0.0, latitude = 0.0),
                ),
            ).map(AISTrace::vesselMMSI) shouldContainExactly listOf(1, 2)
        }
    })

private const val VESSEL_MMSI = 123
private val EPOCH = Instant.fromEpochSeconds(0)

private fun payloadAt(
    seconds: Long,
    vesselMMSI: Int = VESSEL_MMSI,
    longitude: Double? = null,
    latitude: Double? = null,
    headingDegrees: Double? = null,
    shipType: AISShipType? = null,
    callsign: String? = null,
    vesselName: String? = null,
) = AISPayload(
    vesselMMSI = vesselMMSI,
    timestamp = EPOCH + seconds.seconds,
    longitude = longitude,
    latitude = latitude,
    headingDegrees = headingDegrees,
    shipType = shipType,
    callsign = callsign,
    vesselName = vesselName,
)
