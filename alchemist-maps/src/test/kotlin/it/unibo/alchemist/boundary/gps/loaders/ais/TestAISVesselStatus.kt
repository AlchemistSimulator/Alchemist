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
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISVesselStatus :
    StringSpec({
        "AISVesselStatus should discard vessel streams without positions" {
            AISVesselStatus.from(
                listOf(
                    payloadAt(1, vesselMMSI = 1, vesselName = "STATIC"),
                    payloadAt(2, vesselMMSI = 1, callsign = "CALL"),
                ),
            ).shouldBeEmpty()
        }

        "AISVesselStatus should seed the first position-bearing status with earlier static data" {
            val statuses = AISVesselStatus.from(
                listOf(
                    payloadAt(1, vesselName = "VESSEL", shipType = AISShipType.PilotVessel),
                    payloadAt(2, longitude = 10.0, latitude = 44.0),
                ),
            ).getValue(VESSEL_MMSI)
            statuses shouldContainExactly listOf(
                AISVesselStatus(
                    vesselMMSI = VESSEL_MMSI,
                    timestamp = EPOCH + 2.seconds,
                    longitude = 10.0,
                    latitude = 44.0,
                    shipType = AISShipType.PilotVessel,
                    vesselName = "VESSEL",
                ),
            )
        }

        "AISVesselStatus should interpolate numeric measures at non-measure changes" {
            val statuses = AISVesselStatus.from(
                listOf(
                    payloadAt(0, longitude = 0.0, latitude = 0.0, headingDegrees = 10.0),
                    payloadAt(9, shipType = AISShipType.PilotVessel),
                    payloadAt(10, longitude = 10.0, latitude = 10.0, headingDegrees = 20.0),
                ),
            ).getValue(VESSEL_MMSI)
            statuses.map(AISVesselStatus::timestamp) shouldContainExactly listOf(
                EPOCH,
                EPOCH + 9.seconds,
                EPOCH + 10.seconds,
            )
            statuses[1].headingDegrees shouldBe (19.0 plusOrMinus 1e-15)
            statuses[1].longitude shouldBe (9.0 plusOrMinus 1e-15)
            statuses[1].latitude shouldBe (9.0 plusOrMinus 1e-15)
            statuses[1].shipType shouldBe AISShipType.PilotVessel
        }

        "AISVesselStatus should compare by timestamp and then vessel MMSI" {
            val sameTimeHigherMmsi = statusAt(1, vesselMMSI = 2)
            val sameTimeLowerMmsi = statusAt(1, vesselMMSI = 1)
            val later = statusAt(2, vesselMMSI = 0)
            listOf(sameTimeHigherMmsi, later, sameTimeLowerMmsi).sorted() shouldContainExactly listOf(
                sameTimeLowerMmsi,
                sameTimeHigherMmsi,
                later,
            )
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

private fun statusAt(seconds: Long, vesselMMSI: Int) = AISVesselStatus(
    vesselMMSI = vesselMMSI,
    timestamp = EPOCH + seconds.seconds,
    longitude = 0.0,
    latitude = 0.0,
)
