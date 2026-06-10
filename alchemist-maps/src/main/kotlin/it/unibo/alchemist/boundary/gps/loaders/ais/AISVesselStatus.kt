/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import kotlin.math.PI
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.Instant

/**
 * Position-bearing AIS vessel state reconstructed from one or more [AISPayload] events.
 *
 * @property vesselMMSI AIS MMSI.
 * @property timestamp status timestamp.
 * @property latitude latitude of the vessel.
 * @property longitude longitude of the vessel.
 * @property courseOverGroundDegrees course over ground, expressed in degrees.
 * @property speedOverGroundKnots speed over ground, expressed in knots.
 * @property headingDegrees vessel heading, expressed in degrees.
 * @property navigationalStatus AIS navigational status.
 * @property imoNumber IMO number of the vessel.
 * @property vesselName name of the vessel.
 * @property callsign callsign of the vessel.
 * @property shipType AIS ship type.
 * @property dimensionToBow distance from AIS antenna to the bow, in meters.
 * @property dimensionToStern distance from AIS antenna to the stern, in meters.
 * @property dimensionToPort distance from AIS antenna to port, in meters.
 * @property dimensionToStarboard distance from AIS antenna to starboard, in meters.
 * @property draughtMeters current draught, in meters.
 * @property destination destination manually entered in AIS.
 * @property eta estimated time of arrival.
 * @property positionAccuracy raw AIS position accuracy flag.
 * @property rateOfTurn rate of turn, expressed according to the AIS library conversion.
 * @property isEquippedWithRAIM whether AIS reports receiver autonomous integrity monitoring.
 * @property positioningDevice AIS positioning device code.
 * @property dataTerminalReady AIS data terminal ready flag.
 * @property vendorId vendor identifier from AIS static data.
 * @property courseOverGroundRadians course over ground, expressed in radians.
 * @property headingRadians vessel heading, expressed in radians.
 * @property speedOverGroundMetersPerSecond speed over ground, expressed in meters per second.
 */
data class AISVesselStatus(
    val vesselMMSI: Int,
    val timestamp: Instant,
    val latitude: Double?,
    val longitude: Double?,
    val courseOverGroundDegrees: Double? = null,
    val speedOverGroundKnots: Double? = null,
    val headingDegrees: Double? = null,
    val navigationalStatus: AISNavigationStatus? = null,
    val imoNumber: Long? = null,
    val vesselName: String? = null,
    val callsign: String? = null,
    val shipType: AISShipType? = null,
    val dimensionToBow: Int? = null,
    val dimensionToStern: Int? = null,
    val dimensionToPort: Int? = null,
    val dimensionToStarboard: Int? = null,
    val draughtMeters: Double? = null,
    val destination: String? = null,
    val eta: Instant? = null,
    val positionAccuracy: Double? = null,
    val rateOfTurn: Double? = null,
    val isEquippedWithRAIM: Boolean? = null,
    val positioningDevice: Int? = null,
    val dataTerminalReady: Int? = null,
    val vendorId: String? = null,
) : Comparable<AISVesselStatus> {
    val courseOverGroundRadians: Double?
        get() = courseOverGroundDegrees?.degreesToRadians

    val headingRadians: Double?
        get() = headingDegrees?.degreesToRadians

    val speedOverGroundMetersPerSecond: Double?
        get() = speedOverGroundKnots?.knotsToMetersPerSecond

    override fun compareTo(other: AISVesselStatus): Int =
        timestamp.compareTo(other.timestamp).takeUnless { it == 0 } ?: vesselMMSI.compareTo(other.vesselMMSI)

    /**
     * Utilities for compacting AIS payload streams into vessel states.
     */
    companion object {
        private const val METERS_IN_NAUTICAL_MILE = 1_852.0
        private const val SECONDS_PER_HOUR = 3_600.0
        private const val METERS_PER_SECOND_IN_ONE_KNOT = METERS_IN_NAUTICAL_MILE / SECONDS_PER_HOUR

        private val Double.knotsToMetersPerSecond: Double
            get() = this * METERS_PER_SECOND_IN_ONE_KNOT

        private val Double.degreesToRadians: Double
            get() = this * PI / 180.0

        /**
         * Groups payloads by vessel and compacts each vessel stream into status snapshots.
         */
        fun from(payloads: Iterable<AISPayload>): Map<MMSI, List<AISVesselStatus>> = payloads
            .groupBy(AISPayload::vesselMMSI)
            .mapValues { (_, vesselPayloads) -> vesselPayloads.toStatuses() }
            .filterValues(List<AISVesselStatus>::isNotEmpty)

        /**
         * Compacts already grouped AIS payloads into status snapshots.
         */
        fun from(payloads: Map<MMSI, List<AISPayload>>): Map<MMSI, List<AISVesselStatus>> = payloads
            .mapValues { (_, vesselPayloads) -> vesselPayloads.toStatuses() }
            .filterValues(List<AISVesselStatus>::isNotEmpty)

        private fun List<AISPayload>.toStatuses(): List<AISVesselStatus> {
            val sortedPayloads = sorted()
            val firstPositionIndex = sortedPayloads.indexOfFirst(AISPayload::hasPosition)
            if (firstPositionIndex < 0) return emptyList()
            var state = sortedPayloads.take(firstPositionIndex + 1).reduce { state, payload ->
                state.mergedWith(payload)
            }
            return sortedPayloads
                .drop(firstPositionIndex)
                .mapNotNull { payload ->
                    val previous = state
                    state = state.mergedWith(payload)
                    state.statusAt(payload.timestamp, sortedPayloads)
                        .takeIf { previous.differsFrom(state) || payload.hasPosition }
                }
                .distinct()
        }

        private fun AISPayload.mergedWith(payload: AISPayload): AISPayload = copy(
            vesselMMSI = payload.vesselMMSI,
            timestamp = payload.timestamp,
            latitude = payload.latitude ?: latitude,
            longitude = payload.longitude ?: longitude,
            courseOverGroundDegrees = payload.courseOverGroundDegrees ?: courseOverGroundDegrees,
            speedOverGroundKnots = payload.speedOverGroundKnots ?: speedOverGroundKnots,
            headingDegrees = payload.headingDegrees ?: headingDegrees,
            navigationalStatus = payload.navigationalStatus ?: navigationalStatus,
            imoNumber = payload.imoNumber ?: imoNumber,
            vesselName = payload.vesselName ?: vesselName,
            callsign = payload.callsign ?: callsign,
            shipType = payload.shipType ?: shipType,
            dimensionToBow = payload.dimensionToBow ?: dimensionToBow,
            dimensionToStern = payload.dimensionToStern ?: dimensionToStern,
            dimensionToPort = payload.dimensionToPort ?: dimensionToPort,
            dimensionToStarboard = payload.dimensionToStarboard ?: dimensionToStarboard,
            draughtMeters = payload.draughtMeters ?: draughtMeters,
            destination = payload.destination ?: destination,
            eta = payload.eta ?: eta,
            positionAccuracy = payload.positionAccuracy ?: positionAccuracy,
            rateOfTurn = payload.rateOfTurn ?: rateOfTurn,
            isEquippedWithRAIM = payload.isEquippedWithRAIM ?: isEquippedWithRAIM,
            positioningDevice = payload.positioningDevice ?: positioningDevice,
            dataTerminalReady = payload.dataTerminalReady ?: dataTerminalReady,
            vendorId = payload.vendorId ?: vendorId,
        )

        private fun AISPayload.differsFrom(other: AISPayload): Boolean = copy(timestamp = other.timestamp) != other

        private fun AISPayload.statusAt(timestamp: Instant, payloads: List<AISPayload>): AISVesselStatus =
            AISVesselStatus(
                vesselMMSI = vesselMMSI,
                timestamp = timestamp,
                latitude = payloads.interpolated(timestamp, AISPayload::latitude) ?: latitude,
                longitude = payloads.interpolated(timestamp, AISPayload::longitude) ?: longitude,
                courseOverGroundDegrees = payloads.interpolated(timestamp, AISPayload::courseOverGroundDegrees)
                    ?: courseOverGroundDegrees,
                speedOverGroundKnots = payloads.interpolated(timestamp, AISPayload::speedOverGroundKnots)
                    ?: speedOverGroundKnots,
                headingDegrees = payloads.interpolated(timestamp, AISPayload::headingDegrees) ?: headingDegrees,
                navigationalStatus = navigationalStatus,
                imoNumber = imoNumber,
                vesselName = vesselName,
                callsign = callsign,
                shipType = shipType,
                dimensionToBow = dimensionToBow,
                dimensionToStern = dimensionToStern,
                dimensionToPort = dimensionToPort,
                dimensionToStarboard = dimensionToStarboard,
                draughtMeters = draughtMeters,
                destination = destination,
                eta = eta,
                positionAccuracy = positionAccuracy,
                rateOfTurn = payloads.interpolated(timestamp, AISPayload::rateOfTurn) ?: rateOfTurn,
                isEquippedWithRAIM = isEquippedWithRAIM,
                positioningDevice = positioningDevice,
                dataTerminalReady = dataTerminalReady,
                vendorId = vendorId,
            )

        private fun List<AISPayload>.interpolated(timestamp: Instant, selector: (AISPayload) -> Double?): Double? =
            mapNotNull { payload -> selector(payload)?.let { payload.timestamp to it } }
                .takeIf { it.isNotEmpty() }
                ?.let { known ->
                    val previous = known.lastOrNull { (time, _) -> time <= timestamp } ?: known.first()
                    val next = known.firstOrNull { (time, _) -> time >= timestamp } ?: previous
                    when (previous.first) {
                        next.first -> previous.second
                        else -> {
                            val elapsed = (timestamp - previous.first).toDouble(SECONDS)
                            val duration = (next.first - previous.first).toDouble(SECONDS)
                            previous.second + (next.second - previous.second) * elapsed / duration
                        }
                    }
                }
    }
}
