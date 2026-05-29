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
import it.unibo.alchemist.boundary.gps.loaders.ais.AISVesselStatus
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISVessel :
    StringSpec({
        "AISVessel should expose the current AIS data and retain only the latest status by default" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val vessel = AISVessel(node())
            vessel.update(first)
            vessel.update(second)
            vessel.currentStatus shouldBe second.toStatus()
            vessel.history shouldContainExactly listOf(second.toStatus())
        }

        "AISVessel should expose all current AIS packet data" {
            val eta = EPOCH + 42.seconds
            val vessel = AISVessel(node())
            val data = payloadAt(
                0,
                speedOverGroundKnots = 10.0,
                cog = 90.0,
                heading = 180.0,
                positionAccuracy = 1.0,
                rateOfTurn = 2.0,
                navigationalStatus = AISNavigationStatus.RestrictedManoeuverability,
                isEquippedWithRAIM = true,
                shipType = AISShipType.PilotVessel,
                callsign = "CALL",
                vesselName = "VESSEL",
                dimensionToBow = 10,
                dimensionToStern = 20,
                dimensionToPort = 3,
                dimensionToStarboard = 4,
                imoNumber = 1234567,
                positioningDevice = 1,
                eta = eta,
                draughtMeters = 7.5,
                destination = "PORT",
                dataTerminalReady = 0,
                vendorId = "VND",
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
            vessel.isEquippedWithRAIM shouldBe data.isEquippedWithRAIM
            vessel.shipType shouldBe data.shipType
            vessel.callsign shouldBe data.callsign
            vessel.vesselName shouldBe data.vesselName
            vessel.dimensionToBow shouldBe data.dimensionToBow
            vessel.dimensionToStern shouldBe data.dimensionToStern
            vessel.dimensionToPort shouldBe data.dimensionToPort
            vessel.dimensionToStarboard shouldBe data.dimensionToStarboard
            vessel.imoNumber shouldBe data.imoNumber
            vessel.positioningDevice shouldBe data.positioningDevice
            vessel.eta shouldBe data.eta
            vessel.draughtMeters shouldBe data.draughtMeters
            vessel.destination shouldBe data.destination
            vessel.dataTerminalReady shouldBe data.dataTerminalReady
            vessel.vendorId shouldBe data.vendorId
        }

        "AISVessel should fold later non-positional payloads into status snapshots" {
            val positioned = payloadAt(0)
            val static = payloadAt(
                1,
                vesselMMSI = positioned.vesselMMSI,
                longitude = null,
                latitude = null,
                vesselName = "VESSEL",
            )
            val vessel = AISVessel(node(), maxSize = null)
            vessel.update(positioned)
            vessel.update(static)
            vessel.history.map { it.timestamp } shouldContainExactly listOf(static.timestamp, positioned.timestamp)
            vessel.currentStatus.longitude shouldBe positioned.longitude
            vessel.currentStatus.latitude shouldBe positioned.latitude
            vessel.currentStatus.vesselName shouldBe "VESSEL"
        }

        "AISVessel should trim retained AIS data to maxSize" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val third = payloadAt(2)
            val vessel = AISVessel(node(), maxSize = 2)
            listOf(first, second, third).forEach(vessel::update)
            vessel.history shouldContainExactly listOf(third.toStatus(), second.toStatus())
        }

        "AISVessel should discard AIS data outside the validity window" {
            val oldest = payloadAt(0)
            val withinWindow = payloadAt(3)
            val newest = payloadAt(5)
            val lateExpired = payloadAt(1)
            val vessel = AISVessel(node(), maxSize = null, validityWindow = DoubleTime(3.0))
            listOf(oldest, withinWindow, newest, lateExpired).forEach(vessel::update)
            vessel.history shouldContainExactly listOf(newest.toStatus(), withinWindow.toStatus())
        }

        "AISVessel should default unlimited histories to a five-minute validity window" {
            val oldest = payloadAt(0)
            val newest = payloadAt(301)
            val vessel = AISVessel(node(), maxSize = null)
            listOf(oldest, newest).forEach(vessel::update)
            vessel.history shouldContainExactly listOf(newest.toStatus())
        }

        "AISVessel should query retained statuses by simulation time" {
            val first = payloadAt(1)
            val second = payloadAt(2)
            val vessel = AISVessel(node(), maxSize = null)
            listOf(first, second).forEach(vessel::update)
            vessel.statusAt(DoubleTime(1.5)) shouldBe first.toStatus()
            vessel.currentStatus shouldBe second.toStatus()
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
            clone.currentStatus shouldNotBe newest.toStatus()
        }

        "AISVessel should reject invalid retention settings" {
            shouldThrow<IllegalArgumentException> { AISVessel(node(), maxSize = 0) }
            shouldThrow<IllegalArgumentException> { AISVessel(node(), validityWindow = DoubleTime(-1.0)) }
        }
    })

private fun payloadAt(
    seconds: Long,
    vesselMMSI: Int = seconds.toInt(),
    longitude: Double? = seconds.toDouble(),
    latitude: Double? = seconds.toDouble(),
    speedOverGroundKnots: Double? = null,
    cog: Double? = null,
    heading: Double? = null,
    positionAccuracy: Double? = null,
    rateOfTurn: Double? = null,
    navigationalStatus: AISNavigationStatus? = null,
    isEquippedWithRAIM: Boolean? = null,
    shipType: AISShipType? = null,
    callsign: String? = null,
    vesselName: String? = null,
    dimensionToBow: Int? = null,
    dimensionToStern: Int? = null,
    dimensionToPort: Int? = null,
    dimensionToStarboard: Int? = null,
    imoNumber: Long? = null,
    positioningDevice: Int? = null,
    eta: Instant? = null,
    draughtMeters: Double? = null,
    destination: String? = null,
    dataTerminalReady: Int? = null,
    vendorId: String? = null,
) = AISPayload(
    vesselMMSI = vesselMMSI,
    timestamp = EPOCH + seconds.seconds,
    longitude = longitude,
    latitude = latitude,
    speedOverGroundKnots = speedOverGroundKnots,
    courseOverGroundDegrees = cog,
    headingDegrees = heading,
    positionAccuracy = positionAccuracy,
    rateOfTurn = rateOfTurn,
    navigationalStatus = navigationalStatus,
    isEquippedWithRAIM = isEquippedWithRAIM,
    shipType = shipType,
    callsign = callsign,
    vesselName = vesselName,
    dimensionToBow = dimensionToBow,
    dimensionToStern = dimensionToStern,
    dimensionToPort = dimensionToPort,
    dimensionToStarboard = dimensionToStarboard,
    imoNumber = imoNumber,
    positioningDevice = positioningDevice,
    eta = eta,
    draughtMeters = draughtMeters,
    destination = destination,
    dataTerminalReady = dataTerminalReady,
    vendorId = vendorId,
)

private fun AISPayload.toStatus() = AISVesselStatus(
    vesselMMSI = vesselMMSI,
    timestamp = timestamp,
    longitude = longitude ?: error("Test payload must have longitude"),
    latitude = latitude ?: error("Test payload must have latitude"),
    speedOverGroundKnots = speedOverGroundKnots,
    courseOverGroundDegrees = courseOverGroundDegrees,
    headingDegrees = headingDegrees,
    positionAccuracy = positionAccuracy,
    rateOfTurn = rateOfTurn,
    navigationalStatus = navigationalStatus,
    isEquippedWithRAIM = isEquippedWithRAIM,
    shipType = shipType,
    callsign = callsign,
    vesselName = vesselName,
    dimensionToBow = dimensionToBow,
    dimensionToStern = dimensionToStern,
    dimensionToPort = dimensionToPort,
    dimensionToStarboard = dimensionToStarboard,
    imoNumber = imoNumber,
    positioningDevice = positioningDevice,
    eta = eta,
    draughtMeters = draughtMeters,
    destination = destination,
    dataTerminalReady = dataTerminalReady,
    vendorId = vendorId,
)

private val EPOCH = Instant.fromEpochSeconds(0)

private fun node(): Node<Any> = mockk()
