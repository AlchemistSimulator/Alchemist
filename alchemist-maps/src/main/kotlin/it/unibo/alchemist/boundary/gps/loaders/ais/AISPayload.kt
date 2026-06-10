/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisMessage24
import dk.dma.ais.message.AisMessage5
import dk.dma.ais.message.AisPositionMessage
import dk.dma.ais.message.AisStaticCommon
import dk.dma.ais.message.IDimensionMessage
import dk.dma.ais.message.INameMessage
import dk.dma.ais.message.IPositionMessage
import dk.dma.ais.message.IVesselPositionMessage
import dk.dma.ais.message.UTCDateResponseMessage
import kotlin.math.PI
import kotlin.time.Instant

/**
 * Decoded AIS message data.
 *
 * Position data is optional: AIS messages can carry static vessel information without coordinates.
 *
 * @property vesselMMSI AIS MMSI.
 * @property timestamp the timestamp related to the receipt or content of the message.
 * @property latitude latitude of the vessel, when available.
 * @property longitude longitude of the vessel, when available.
 * @property courseOverGroundDegrees course over ground, expressed in degrees.
 * @property courseOverGroundRadians course over ground, expressed in radians.
 * @property speedOverGroundKnots speed over ground, expressed in knots.
 * @property speedOverGroundMetersPerSecond speed over ground, expressed in meters per second.
 * @property headingDegrees vessel heading, expressed in degrees.
 * @property headingRadians vessel heading, expressed in radians.
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
 * @property hasPosition whether both latitude and longitude are available.
 */
data class AISPayload(
    val vesselMMSI: Int,
    val timestamp: Instant,
    val latitude: Double? = null,
    val longitude: Double? = null,
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
) : Comparable<AISPayload> {

    val hasPosition: Boolean
        get() = longitude != null && latitude != null

    val courseOverGroundRadians: Double?
        get() = courseOverGroundDegrees?.degreesToRadians

    val headingRadians: Double?
        get() = headingDegrees?.degreesToRadians

    val speedOverGroundMetersPerSecond: Double?
        get() = speedOverGroundKnots?.knotsToMetersPerSecond

    override fun compareTo(other: AISPayload): Int = compareBy<AISPayload> { timestamp }
        .thenBy { vesselMMSI }
        .compare(this, other)

    /**
     * Static factory for [AISPayload].
     */
    companion object {
        /** AIS stores speed over ground, course over ground, and draught in tenths of their respective units. */
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

        private val Double.degreesToRadians: Double
            get() = this * PI / 180.0

        /**
         * Builds an [AISPayload] from an AIS message using the timestamp carried by the message, when available.
         */
        fun fromSingleMessage(message: AisMessage): AISPayload? =
            message.timestamp?.let { fromTimedMessages(it, message) }

        /**
         * Builds an [AISPayload] from an AIS message.
         */
        fun fromTimedMessages(timestamp: Instant, message: AisMessage): AISPayload {
            val positionMessage = message as? IPositionMessage
            val vesselPosition = message as? IVesselPositionMessage
            val aisPosition = message as? AisPositionMessage
            val staticCommon = message as? AisStaticCommon
            val dimensions = message as? IDimensionMessage
            val name = message as? INameMessage
            val message5 = message as? AisMessage5
            val message24 = message as? AisMessage24
            return AISPayload(
                vesselMMSI = message.userId,
                timestamp = timestamp,
                latitude = positionMessage?.latitude,
                longitude = positionMessage?.longitude,
                courseOverGroundDegrees = vesselPosition?.takeIf { it.isCogValid }?.cog?.div(AIS_TENTHS_SCALE),
                speedOverGroundKnots = vesselPosition?.takeIf { it.isSogValid }?.sog?.div(AIS_TENTHS_SCALE),
                headingDegrees = vesselPosition?.takeIf { it.isHeadingValid }?.trueHeading?.toDouble(),
                navigationalStatus = message.navigationStatus,
                imoNumber = message5?.imo?.takeUnless { it == 0L },
                vesselName = name?.name?.trimText(),
                callsign = staticCommon?.callsign?.trimText(),
                shipType = staticCommon?.shipType?.let(AISShipType::fromCode),
                dimensionToBow = dimensions?.dimBow,
                dimensionToStern = dimensions?.dimStern,
                dimensionToPort = dimensions?.dimPort,
                dimensionToStarboard = dimensions?.dimStarboard,
                draughtMeters = message5?.draught?.div(AIS_TENTHS_SCALE),
                destination = message5?.dest?.trimText(),
                eta = message5?.etaDate?.time?.let(Instant::fromEpochMilliseconds),
                positionAccuracy = positionMessage?.posAcc?.toDouble(),
                rateOfTurn = aisPosition?.takeIf { it.isRotValid }?.rot?.toDouble(),
                isEquippedWithRAIM = message.isEquippedWithRAIM,
                positioningDevice = message5?.posType ?: message24?.posType,
                dataTerminalReady = message5?.dte,
                vendorId = message24?.vendorId?.trimText(),
            )
        }

        /**
         * Converts timestamped AIS messages to payloads.
         */
        fun fromTimedMessages(messages: Iterable<Pair<Instant, AisMessage>>): Map<MMSI, List<AISPayload>> = messages
            .map { (timestamp, message) -> fromTimedMessages(timestamp, message) }
            .groupBy(AISPayload::vesselMMSI)
            .mapValues { (_, payloads) -> payloads.sorted() }

        /**
         * Converts AIS messages with embedded timestamps to payloads.
         */
        fun fromMessages(messages: Iterable<AisMessage>): Map<MMSI, List<AISPayload>> = messages
            .mapNotNull(::fromSingleMessage)
            .groupBy(AISPayload::vesselMMSI)
            .mapValues { (_, payloads) -> payloads.sorted() }

        private val AisMessage.timestamp: Instant?
            get() = sourceTag?.timestamp?.time?.let(Instant::fromEpochMilliseconds)
                ?: (this as? UTCDateResponseMessage)?.date?.time?.let(Instant::fromEpochMilliseconds)

        private val AisMessage.navigationStatus: AISNavigationStatus?
            get() = when (this) {
                is AisPositionMessage -> navStatus
                is dk.dma.ais.message.AisMessage27 -> navStatus
                else -> null
            }?.let(AISNavigationStatus::fromCode)

        private val AisMessage.isEquippedWithRAIM: Boolean?
            get() = when (this) {
                is IVesselPositionMessage -> raim
                is UTCDateResponseMessage -> raim
                is dk.dma.ais.message.AisMessage27 -> raim
                else -> null
            }?.let { it == AIS_FLAG_SET }

        private val IPositionMessage.longitude: Double?
            get() = pos.longitudeDouble.takeIf { it < AIS_LONGITUDE_UNAVAILABLE }

        private val IPositionMessage.latitude: Double?
            get() = pos.latitudeDouble.takeIf { it < AIS_LATITUDE_UNAVAILABLE }

        private fun String.trimText(): String? = AisMessage.trimText(this).takeUnless(String::isBlank)

        private const val AIS_FLAG_SET = 1
    }
}
