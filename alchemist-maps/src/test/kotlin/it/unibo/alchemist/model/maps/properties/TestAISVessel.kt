/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.properties

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import it.unibo.alchemist.boundary.gps.loaders.ais.AISNavigationStatus
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.boundary.gps.loaders.ais.AISShipType
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISVessel :
    StringSpec({
        "AISVessel should expose the current AIS data and retain history in update order" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val vessel = AISVessel(node())
            vessel.update(first)
            vessel.update(second)
            vessel.currentData shouldBe second
            vessel.history shouldContainExactly listOf(second, first)
        }

        "AISVessel should expose all current AIS packet data" {
            val vessel = AISVessel(node())
            val data = payloadAt(
                0,
                speedOverGroundKnots = 10.0,
                cog = 90.0,
                heading = 180.0,
                positionAccuracy = 1.0,
                rateOfTurn = 2.0,
                navigationalStatus = AISNavigationStatus.RestrictedManoeuverability,
                raim = 4.0,
                shipType = AISShipType.PilotVessel,
            )
            vessel.update(data)
            vessel.vesselId shouldBe data.vesselMMSI
            vessel.timestamp shouldBe data.timestamp
            vessel.longitude shouldBe data.longitude
            vessel.latitude shouldBe data.latitude
            vessel.speedOverGroundKnots shouldBe data.speedOverGroundKnots
            vessel.speedOverGroundMetersPerSecond shouldBe 5.144444444444445
            vessel.courseOverGroundDegrees shouldBe data.courseOverGroundDegrees
            vessel.courseOverGroundRadians shouldBe data.courseOverGroundRadians
            vessel.headingDegrees shouldBe data.headingDegrees
            vessel.headingRadians shouldBe data.headingRadians
            vessel.positionAccuracy shouldBe data.positionAccuracy
            vessel.rateOfTurn shouldBe data.rateOfTurn
            vessel.navigationalStatus shouldBe data.navigationalStatus
            vessel.raim shouldBe data.raim
            vessel.shipType shouldBe data.shipType
        }

        "AISVessel should trim retained AIS data to maxSize" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val third = payloadAt(2)
            val vessel = AISVessel(node(), maxSize = 2)
            listOf(first, second, third).forEach(vessel::update)
            vessel.history shouldContainExactly listOf(third, second)
        }

        "AISVessel should discard AIS data outside the validity window" {
            val oldest = payloadAt(0)
            val withinWindow = payloadAt(3)
            val newest = payloadAt(5)
            val lateExpired = payloadAt(1)
            val vessel = AISVessel(node(), validityWindow = DoubleTime(3.0))
            listOf(oldest, withinWindow, newest, lateExpired).forEach(vessel::update)
            vessel.history shouldContainExactly listOf(newest, withinWindow)
        }

        "AISVessel should clone retained AIS data and trimming policy" {
            val newest = payloadAt(2)
            val vessel = AISVessel(node(), maxSize = 1).apply {
                update(payloadAt(1))
                update(newest)
            }
            val clone = vessel.cloneOnNewNode(node()) as AISVessel<Any>
            clone.update(payloadAt(3))
            clone.history.size shouldBe 1
            clone.currentData shouldNotBe newest
        }

        "AISVessel should reject invalid retention settings" {
            shouldThrow<IllegalArgumentException> { AISVessel(node(), maxSize = 0) }
            shouldThrow<IllegalArgumentException> { AISVessel(node(), validityWindow = DoubleTime(-1.0)) }
        }
    })

private fun payloadAt(
    seconds: Long,
    speedOverGroundKnots: Double? = null,
    cog: Double? = null,
    heading: Double? = null,
    positionAccuracy: Double? = null,
    rateOfTurn: Double? = null,
    navigationalStatus: AISNavigationStatus? = null,
    raim: Double? = null,
    shipType: AISShipType? = null,
) = AISPayload(
    vesselMMSI = seconds.toInt(),
    timestamp = EPOCH + seconds.seconds,
    longitude = seconds.toDouble(),
    latitude = seconds.toDouble(),
    speedOverGroundKnots = speedOverGroundKnots,
    courseOverGroundDegrees = cog,
    headingDegrees = heading,
    positionAccuracy = positionAccuracy,
    rateOfTurn = rateOfTurn,
    navigationalStatus = navigationalStatus,
    raim = raim,
    shipType = shipType,
)

private val EPOCH = Instant.fromEpochSeconds(0)

private fun node(): Node<Any> = mockk()
