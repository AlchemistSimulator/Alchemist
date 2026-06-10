/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.properties

import dk.dma.ais.message.AisMessage
import it.unibo.alchemist.boundary.gps.loaders.ais.AISNavigationStatus
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.boundary.gps.loaders.ais.AISShipType
import it.unibo.alchemist.boundary.gps.loaders.ais.AISVesselStatus
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.Instant

/**
 * Node property exposing AIS data associated with the node.
 *
 * The node is intended to represent the vessel described by the latest AIS vessel status, while [history] keeps
 * previous statuses when retention is enabled. The AIS vessel identifier is exposed through [vesselId].
 *
 * @param maxSize maximum number of AIS vessel statuses to retain in [history].
 *   Defaults to 1. Use `null` to retain all statuses without any size limit.
 * @param validityWindow Alchemist time window for retained statuses in [history].
 */
class AISVessel<T> @JvmOverloads constructor(
    node: Node<T>,
    private val maxSize: Int? = 1,
    private val validityWindow: Time? = DoubleTime(DEFAULT_VALIDITY_WINDOW_SECONDS),
) : AbstractNodeProperty<T>(node) {
    init {
        require(maxSize == null || maxSize > 0) { "maxSize must be positive or null for unlimited retention" }
        require(validityWindow?.toDouble()?.let { it >= 0.0 } != false) {
            "validityWindow must not be negative"
        }
    }

    private val statuses = ArrayDeque<AISVesselStatus>()
    private val payloads = ArrayDeque<AISPayload>()

    /**
     * Retained AIS vessel statuses associated with this node, from newest to oldest.
     */
    val history: List<AISVesselStatus>
        get() = statuses.toList()

    /**
     * Latest AIS vessel status associated with this node.
     */
    val currentStatus: AISVesselStatus
        get() = statuses.firstOrNull() ?: error("AISVessel has no valid status")

    /**
     * AIS MMSI of the vessel represented by this node.
     */
    val vesselId: Int
        get() = currentStatus.vesselMMSI

    /**
     * Timestamp of the current AIS payload.
     */
    val timestamp: Instant
        get() = currentStatus.timestamp

    /**
     * Current AIS speed over ground, expressed in knots.
     */
    val speedOverGroundKnots: Double?
        get() = currentStatus.speedOverGroundKnots

    /**
     * Current AIS speed over ground, expressed in meters per second.
     */
    val speedOverGroundMetersPerSecond: Double?
        get() = currentStatus.speedOverGroundMetersPerSecond

    /**
     * Current AIS course over ground, expressed in degrees.
     */
    val courseOverGroundDegrees: Double?
        get() = currentStatus.courseOverGroundDegrees

    /**
     * Current AIS course over ground, expressed in radians.
     */
    val courseOverGroundRadians: Double?
        get() = currentStatus.courseOverGroundRadians

    /**
     * Current AIS vessel heading, expressed in degrees.
     */
    val headingDegrees: Double?
        get() = currentStatus.headingDegrees

    /**
     * Current AIS vessel heading, expressed in radians.
     */
    val headingRadians: Double?
        get() = currentStatus.headingRadians

    /**
     * Current raw AIS position accuracy flag.
     */
    val positionAccuracy: Double?
        get() = currentStatus.positionAccuracy

    /**
     * Current AIS rate of turn.
     */
    val rateOfTurn: Double?
        get() = currentStatus.rateOfTurn

    /**
     * Current AIS navigational status.
     */
    val navigationalStatus: AISNavigationStatus?
        get() = currentStatus.navigationalStatus

    /**
     * Whether the current AIS data reports receiver autonomous integrity monitoring.
     */
    val isEquippedWithRAIM: Boolean?
        get() = currentStatus.isEquippedWithRAIM

    /**
     * Current AIS ship type.
     */
    val shipType: AISShipType?
        get() = currentStatus.shipType

    /**
     * Current AIS callsign.
     */
    val callsign: String?
        get() = currentStatus.callsign

    /**
     * Current AIS vessel name.
     */
    val vesselName: String?
        get() = currentStatus.vesselName

    /**
     * Current AIS dimension to bow, in meters.
     */
    val dimensionToBow: Int?
        get() = currentStatus.dimensionToBow

    /**
     * Current AIS dimension to stern, in meters.
     */
    val dimensionToStern: Int?
        get() = currentStatus.dimensionToStern

    /**
     * Current AIS dimension to port, in meters.
     */
    val dimensionToPort: Int?
        get() = currentStatus.dimensionToPort

    /**
     * Current AIS dimension to starboard, in meters.
     */
    val dimensionToStarboard: Int?
        get() = currentStatus.dimensionToStarboard

    /**
     * Current AIS IMO number.
     */
    val imoNumber: Long?
        get() = currentStatus.imoNumber

    /**
     * Current AIS positioning device code.
     */
    val positioningDevice: Int?
        get() = currentStatus.positioningDevice

    /**
     * Current AIS estimated time of arrival.
     */
    val eta: Instant?
        get() = currentStatus.eta

    /**
     * Current AIS draught, in meters.
     */
    val draughtMeters: Double?
        get() = currentStatus.draughtMeters

    /**
     * Current AIS destination.
     */
    val destination: String?
        get() = currentStatus.destination

    /**
     * Current AIS data terminal ready flag.
     */
    val dataTerminalReady: Int?
        get() = currentStatus.dataTerminalReady

    /**
     * Current AIS vendor identifier.
     */
    val vendorId: String?
        get() = currentStatus.vendorId

    /**
     * Updates this node's AIS data and stores the status in [history].
     *
     * @param status the AIS vessel status associated with this node.
     */
    fun update(status: AISVesselStatus) {
        statuses.addFirst(status)
        trim()
    }

    /**
     * Updates this node's AIS data from compactable payloads.
     */
    fun update(data: Iterable<AISPayload>) {
        payloads.addAll(data)
        rebuildStatuses()
    }

    /**
     * Updates this node's AIS data from a compactable payload.
     */
    fun update(payload: AISPayload) {
        update(listOf(payload))
    }

    /**
     * Converts an AIS message into payload data and updates this node when it can produce vessel statuses.
     *
     * @param timestamp the timestamp of the AIS message.
     * @param message the AIS message to use as this node's data.
     */
    fun update(timestamp: Instant, message: AisMessage) {
        update(listOf(AISPayload.fromTimedMessages(timestamp, message)))
    }

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = AISVessel(node, maxSize, validityWindow).also {
        it.statuses.addAll(statuses)
        it.payloads.addAll(payloads)
    }

    /**
     * Returns the latest known status at [time].
     */
    fun statusAt(time: Time): AISVesselStatus = statuses
        .filter { status -> status.timestamp.toSimulationTime().toDouble() <= time.toDouble() }
        .maxByOrNull(AISVesselStatus::timestamp)
        ?: statuses.minByOrNull(AISVesselStatus::timestamp)
        ?: error("AISVessel has no valid status")

    private fun trim() {
        maxSize?.let { max ->
            while (statuses.size > max) {
                statuses.removeLast()
            }
        }
        validityWindow?.let { window ->
            statuses
                .maxOfOrNull(AISVesselStatus::timestamp)
                ?.let { newestTimestamp ->
                    statuses.removeAll { status ->
                        (newestTimestamp - status.timestamp).toDouble(SECONDS) > window.toDouble()
                    }
                    payloads.removeAll { payload ->
                        (newestTimestamp - payload.timestamp).toDouble(SECONDS) > window.toDouble()
                    }
                }
        }
    }

    private fun rebuildStatuses() {
        statuses.clear()
        AISVesselStatus.from(payloads)
            .values
            .flatten()
            .sortedDescending()
            .forEach(statuses::addLast)
        trim()
    }

    private fun Instant.toSimulationTime(): Time = DoubleTime(toEpochMilliseconds() / 1_000.0)

    private companion object {
        private const val DEFAULT_VALIDITY_WINDOW_SECONDS = 5.0 * 60
    }
}
