/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import it.unibo.alchemist.model.Time
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.Instant
import org.slf4j.LoggerFactory

/**
 * Simulation-aligned AIS payload stream for a single vessel.
 *
 * @property vesselMMSI AIS MMSI assigned to this trace.
 * @property payloads progressively completed AIS payloads sorted by timestamp.
 */
class AISTrace(
    val vesselMMSI: MMSI,
    val payloads: List<AISPayload>,
    private val rawPayloads: List<AISPayload> = payloads,
) : Comparable<AISTrace> {
    init {
        require(payloads.isNotEmpty()) { "AIS trace for vessel $vesselMMSI has no payloads" }
        require(payloads.all { it.vesselMMSI == vesselMMSI }) { "AIS trace contains multiple MMSIs" }
        require(rawPayloads.all { it.vesselMMSI == vesselMMSI }) { "AIS trace raw payloads contain multiple MMSIs" }
    }

    internal val positionPayloads: List<AISPayload>
        get() = rawPayloads.filter(AISPayload::hasPosition).sortedBy(AISPayload::timestamp)

    /**
     * Current AIS payload at simulation [time].
     */
    fun payloadAt(time: Time): AISPayload = payloadAt(EPOCH + time.toDouble().seconds)

    /**
     * Current AIS payload at [timestamp].
     */
    fun payloadAt(timestamp: Instant): AISPayload = AISPayload(
        vesselMMSI = vesselMMSI,
        timestamp = timestamp,
        latitude = rawPayloads.interpolated(timestamp, AISPayload::latitude),
        longitude = rawPayloads.interpolated(timestamp, AISPayload::longitude),
        courseOverGroundDegrees = rawPayloads.interpolated(timestamp, AISPayload::courseOverGroundDegrees),
        speedOverGroundKnots = rawPayloads.interpolated(timestamp, AISPayload::speedOverGroundKnots),
        headingDegrees = rawPayloads.interpolated(timestamp, AISPayload::headingDegrees),
        navigationalStatus = payloads.closest(timestamp, AISPayload::navigationalStatus),
        imoNumber = payloads.closest(timestamp, AISPayload::imoNumber),
        vesselName = payloads.closest(timestamp, AISPayload::vesselName),
        callsign = payloads.closest(timestamp, AISPayload::callsign),
        shipType = payloads.closest(timestamp, AISPayload::shipType),
        dimensionToBow = payloads.closest(timestamp, AISPayload::dimensionToBow),
        dimensionToStern = payloads.closest(timestamp, AISPayload::dimensionToStern),
        dimensionToPort = payloads.closest(timestamp, AISPayload::dimensionToPort),
        dimensionToStarboard = payloads.closest(timestamp, AISPayload::dimensionToStarboard),
        draughtMeters = rawPayloads.interpolated(timestamp, AISPayload::draughtMeters),
        destination = payloads.closest(timestamp, AISPayload::destination),
        eta = payloads.closest(timestamp, AISPayload::eta),
        positionAccuracy = payloads.closest(timestamp, AISPayload::positionAccuracy),
        rateOfTurn = rawPayloads.interpolated(timestamp, AISPayload::rateOfTurn),
        isEquippedWithRAIM = payloads.closest(timestamp, AISPayload::isEquippedWithRAIM),
        positioningDevice = payloads.closest(timestamp, AISPayload::positioningDevice),
        dataTerminalReady = payloads.closest(timestamp, AISPayload::dataTerminalReady),
        vendorId = payloads.closest(timestamp, AISPayload::vendorId),
    )

    override fun compareTo(other: AISTrace): Int = vesselMMSI.compareTo(other.vesselMMSI)

    internal fun shiftedBy(shift: Duration): AISTrace = AISTrace(
        vesselMMSI,
        payloads.map { it.copy(timestamp = it.timestamp + shift) },
        rawPayloads.map { it.copy(timestamp = it.timestamp + shift) },
    )

    /**
     * AIS trace factory.
     */
    companion object {
        private val EPOCH = Instant.fromEpochSeconds(0)

        /**
         * Groups raw payloads into progressively completed, position-bearing AIS traces.
         */
        fun from(payloads: Iterable<AISPayload>): List<AISTrace> = payloads
            .groupBy(AISPayload::vesselMMSI)
            .mapValues { (mmsi, vesselPayloads) ->
                val rawPayloads = vesselPayloads.sortedBy(AISPayload::timestamp)
                rawPayloads to rawPayloads.progressivelyComplete(mmsi)
            }
            .filterValues { (rawPayloads, _) -> rawPayloads.any(AISPayload::hasPosition) }
            .map { (mmsi, vesselPayloads) -> AISTrace(mmsi, vesselPayloads.second, vesselPayloads.first) }
            .sorted()
    }
}

private fun List<AISPayload>.progressivelyComplete(mmsi: MMSI): List<AISPayload> {
    val seen = mutableMapOf<String, Any>()
    return sortedBy(AISPayload::timestamp).runningFold<AISPayload, AISPayload?>(null) { previous, payload ->
        previous?.warnUniqueFieldChanges(payload, seen) ?: payload.warnUniqueFieldChanges(mmsi, seen)
        previous?.mergedWith(payload) ?: payload
    }.filterNotNull()
}

private fun AISPayload.warnUniqueFieldChanges(mmsi: MMSI, seen: MutableMap<String, Any>): AISPayload = also {
    uniqueFields.forEach { (name, value) ->
        value(this)?.let { current ->
            seen.putIfAbsent(name, current)?.takeIf { previous -> previous != current }?.let { previous ->
                LOGGER.warn(
                    "AIS vessel {} changed unique field {} from {} to {} at {}",
                    mmsi,
                    name,
                    previous,
                    current,
                    timestamp,
                )
            }
        }
    }
}

private fun AISPayload.warnUniqueFieldChanges(payload: AISPayload, seen: MutableMap<String, Any>): AISPayload =
    payload.warnUniqueFieldChanges(vesselMMSI, seen)

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

private fun List<AISPayload>.interpolated(timestamp: Instant, selector: (AISPayload) -> Double?): Double? =
    mapNotNull { payload -> selector(payload)?.let { payload.timestamp to it } }
        .takeIf { it.isNotEmpty() }
        ?.let { known ->
            val previous = known.lastOrNull { (time, _) -> time <= timestamp }
            val next = known.firstOrNull { (time, _) -> time >= timestamp }
            when {
                previous == null -> next?.second
                next == null -> previous.second
                previous.first == next.first -> previous.second
                else -> {
                    val elapsed = (timestamp - previous.first).toDouble(SECONDS)
                    val duration = (next.first - previous.first).toDouble(SECONDS)
                    previous.second + (next.second - previous.second) * elapsed / duration
                }
            }
        }

private fun <T : Any> List<AISPayload>.closest(timestamp: Instant, selector: (AISPayload) -> T?): T? =
    mapNotNull { payload -> selector(payload)?.let { payload.timestamp to it } }
        .minWithOrNull(
            compareBy<Pair<Instant, T>> { abs((it.first - timestamp).toDouble(SECONDS)) }.thenBy { it.first },
        )
        ?.second

private val uniqueFields: List<Pair<String, (AISPayload) -> Any?>> = listOf(
    "imoNumber" to AISPayload::imoNumber,
    "dimensionToBow" to AISPayload::dimensionToBow,
    "dimensionToStern" to AISPayload::dimensionToStern,
    "dimensionToPort" to AISPayload::dimensionToPort,
    "dimensionToStarboard" to AISPayload::dimensionToStarboard,
)

private val LOGGER = LoggerFactory.getLogger(AISTrace::class.java)
