/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisPositionMessage
import dk.dma.ais.message.AisStaticCommon
import dk.dma.ais.message.IPositionMessage
import dk.dma.ais.message.IVesselPositionMessage
import kotlin.time.Instant

/**
 * Subset of AIS information used to generate traces.
 *
 * @property vesselId AIS MMSI.
 * @property timestamp the timestamp related to the receipt of the message.
 * @property longitude longitude of the boat.
 * @property latitude latitude of the boat.
 * @property speedOverGroundKnots speed over ground, expressed in knots.
 * @property speedOverGroundMetersPerSecond speed over ground, expressed in meters per second.
 * @property courseOverGround course over ground, expressed in degrees.
 * @property heading vessel heading, expressed in degrees.
 * @property positionAccuracy raw AIS position accuracy flag.
 * @property rateOfTurn rate of turn, expressed according to the AIS library conversion.
 * @property navigationalStatus raw AIS navigational status.
 * @property raim raw AIS RAIM flag.
 * @property shipType vessel type.
 */
data class AISPayload(
    val vesselId: Int,
    val timestamp: Instant,
    val longitude: Double,
    val latitude: Double,
    val speedOverGroundKnots: Double? = null,
    val courseOverGround: Double? = null,
    val heading: Double? = null,
    val positionAccuracy: Double? = null,
    val rateOfTurn: Double? = null,
    val navigationalStatus: Double? = null,
    val raim: Double? = null,
    val shipType: Double? = null,
) {
    val speedOverGroundMetersPerSecond: Double?
        get() = speedOverGroundKnots?.knotsToMetersPerSecond

    /**
     * Static factory for [AISPayload].
     */
    companion object {
        /** AIS stores speed over ground and course over ground as tenths of knots/degrees. */
        private const val AIS_TENTHS_SCALE = 10.0
        private const val METERS_IN_NAUTICAL_MILE = 1_852.0
        private const val SECONDS_PER_HOUR = 3_600.0
        private const val METERS_PER_SECOND_IN_ONE_KNOT = METERS_IN_NAUTICAL_MILE / SECONDS_PER_HOUR

        /** AIS sentinel value indicating that longitude is unavailable. */
        private const val AIS_LONGITUDE_UNAVAILABLE = 181.0

        /** AIS sentinel value indicating that latitude is unavailable. */
        private const val AIS_LATITUDE_UNAVAILABLE = 91.0

        private val Double.knotsToMetersPerSecond: Double
            get() = this * METERS_PER_SECOND_IN_ONE_KNOT

        /**
         * Builds an [AISPayload] from an AIS message when it carries a valid position.
         */
        fun from(timestamp: Instant, message: AisMessage): AISPayload? = when (message) {
            is IPositionMessage -> from(timestamp, message.userId, message)
            else -> null
        }

        /**
         * Builds an [AISPayload] from an AIS message when it carries a valid position.
         * Returns null if the position uses the standard unavailable coordinate sentinels
         * (181° longitude or 91° latitude).
         */
        fun from(timestamp: Instant, userId: Int, positionMessage: IPositionMessage): AISPayload? {
            val longitude = positionMessage.pos.longitudeDouble
            val latitude = positionMessage.pos.latitudeDouble
            if (longitude >= AIS_LONGITUDE_UNAVAILABLE || latitude >= AIS_LATITUDE_UNAVAILABLE) return null
            val vesselPosition = positionMessage as? IVesselPositionMessage
            val aisPosition = positionMessage as? AisPositionMessage
            return AISPayload(
                vesselId = userId,
                timestamp = timestamp,
                longitude = longitude,
                latitude = latitude,
                speedOverGroundKnots = vesselPosition?.takeIf { it.isSogValid }?.sog?.div(AIS_TENTHS_SCALE),
                courseOverGround = vesselPosition?.takeIf { it.isCogValid }?.cog?.div(AIS_TENTHS_SCALE),
                heading = vesselPosition?.takeIf { it.isHeadingValid }?.trueHeading?.toDouble(),
                positionAccuracy = vesselPosition?.posAcc?.toDouble(),
                rateOfTurn = aisPosition?.takeIf { it.isRotValid }?.rot?.toDouble(),
                navigationalStatus = aisPosition?.navStatus?.toDouble(),
                raim = vesselPosition?.raim?.toDouble(),
                shipType = (positionMessage as? AisStaticCommon)?.shipType?.toDouble(),
            )
        }

        /**
         * Converts timestamped AIS messages to payloads.
         */
        fun from(messages: Iterable<Pair<Instant, AisMessage>>): List<AISPayload> = messages
            .mapNotNull { (timestamp, message) -> from(timestamp, message) }
            .sortedWith(compareBy(AISPayload::vesselId, AISPayload::timestamp))
    }
}
