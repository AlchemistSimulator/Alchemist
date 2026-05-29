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
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import kotlin.math.PI
import kotlin.time.Instant

class TestAISPayload :
    StringSpec({
        "AISPayload should convert speed from knots to meters per second" {
            val speedOverGroundKnots = 10.0
            val payload = AISPayload(
                vesselMMSI = 1,
                timestamp = EPOCH,
                longitude = 11.0,
                latitude = 44.0,
                speedOverGroundKnots = speedOverGroundKnots,
            )
            payload.speedOverGroundMetersPerSecond shouldBe
                speedOverGroundKnots * METERS_IN_NAUTICAL_MILE / SECONDS_PER_HOUR
        }

        "AISPayload should keep missing speed unavailable" {
            val payload = AISPayload(
                vesselMMSI = 1,
                timestamp = EPOCH,
                longitude = 11.0,
                latitude = 44.0,
            )
            payload.speedOverGroundMetersPerSecond shouldBe null
        }

        "AISPayload should convert course over ground from degrees to radians" {
            val payload = AISPayload(
                vesselMMSI = 1,
                timestamp = EPOCH,
                longitude = 11.0,
                latitude = 44.0,
                courseOverGroundDegrees = 180.0,
            )
            payload.courseOverGroundRadiants shouldBe (PI plusOrMinus 1e-15)
        }

        "AISShipType should map AIS ship type codes to semantic values" {
            AISShipType.fromCode(0) shouldBe AISShipType.NotAvailable
            AISShipType.fromCode(19) shouldBe AISShipType.ReservedForFutureUse(19)
            AISShipType.fromCode(50) shouldBe AISShipType.PilotVessel
            AISShipType.fromCode(71) shouldBe AISShipType.HazardousCargoA
            AISShipType.fromCode(99) shouldBe AISShipType.OtherTypeNoAdditionalInformation
            AISShipType.fromCode(100) shouldBe null
        }
    })

private val EPOCH = Instant.fromEpochSeconds(0)
private const val METERS_IN_NAUTICAL_MILE = 1_852.0
private const val SECONDS_PER_HOUR = 3_600.0
