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
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.boundary.gps.loaders.ais.AISShipType
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISComm :
    StringSpec({
        "AISComm should expose the current AIS data and retain history in update order" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val comm = AISComm(node())
            comm.update(first)
            comm.update(second)
            comm.currentData shouldBe second
            comm.history shouldContainExactly listOf(second, first)
        }

        "AISComm should expose all current AIS packet data" {
            val comm = AISComm(node())
            val data = payloadAt(
                0,
                speedOverGroundKnots = 10.0,
                cog = 90.0,
                heading = 180.0,
                positionAccuracy = 1.0,
                rateOfTurn = 2.0,
                navigationalStatus = 3.0,
                raim = 4.0,
                shipType = AISShipType.PilotVessel,
            )
            comm.update(data)
            comm.vesselId shouldBe data.vesselMMSI
            comm.timestamp shouldBe data.timestamp
            comm.longitude shouldBe data.longitude
            comm.latitude shouldBe data.latitude
            comm.speedOverGroundKnots shouldBe data.speedOverGroundKnots
            comm.speedOverGroundMetersPerSecond shouldBe 5.144444444444445
            comm.courseOverGround shouldBe data.courseOverGroundDegrees
            comm.heading shouldBe data.heading
            comm.positionAccuracy shouldBe data.positionAccuracy
            comm.rateOfTurn shouldBe data.rateOfTurn
            comm.navigationalStatus shouldBe data.navigationalStatus
            comm.raim shouldBe data.raim
            comm.shipType shouldBe data.shipType
        }

        "AISComm should trim retained AIS data to maxSize" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val third = payloadAt(2)
            val comm = AISComm(node(), maxSize = 2)
            listOf(first, second, third).forEach(comm::update)
            comm.history shouldContainExactly listOf(third, second)
        }

        "AISComm should discard AIS data outside the validity window" {
            val oldest = payloadAt(0)
            val withinWindow = payloadAt(3)
            val newest = payloadAt(5)
            val lateExpired = payloadAt(1)
            val comm = AISComm(node(), validityWindow = DoubleTime(3.0))
            listOf(oldest, withinWindow, newest, lateExpired).forEach(comm::update)
            comm.history shouldContainExactly listOf(newest, withinWindow)
        }

        "AISComm should clone retained AIS data and trimming policy" {
            val newest = payloadAt(2)
            val comm = AISComm(node(), maxSize = 1).apply {
                update(payloadAt(1))
                update(newest)
            }
            val clone = comm.cloneOnNewNode(node()) as AISComm<Any>
            clone.update(payloadAt(3))
            clone.history.size shouldBe 1
            clone.currentData shouldNotBe newest
        }

        "AISComm should reject invalid retention settings" {
            shouldThrow<IllegalArgumentException> { AISComm(node(), maxSize = 0) }
            shouldThrow<IllegalArgumentException> { AISComm(node(), validityWindow = DoubleTime(-1.0)) }
        }
    })

private fun payloadAt(
    seconds: Long,
    speedOverGroundKnots: Double? = null,
    cog: Double? = null,
    heading: Double? = null,
    positionAccuracy: Double? = null,
    rateOfTurn: Double? = null,
    navigationalStatus: Double? = null,
    raim: Double? = null,
    shipType: AISShipType? = null,
) = AISPayload(
    vesselMMSI = seconds.toInt(),
    timestamp = EPOCH + seconds.seconds,
    longitude = seconds.toDouble(),
    latitude = seconds.toDouble(),
    speedOverGroundKnots = speedOverGroundKnots,
    courseOverGroundDegrees = cog,
    heading = heading,
    positionAccuracy = positionAccuracy,
    rateOfTurn = rateOfTurn,
    navigationalStatus = navigationalStatus,
    raim = raim,
    shipType = shipType,
)

private val EPOCH = Instant.fromEpochSeconds(0)

private fun node(): Node<Any> = mockk()
