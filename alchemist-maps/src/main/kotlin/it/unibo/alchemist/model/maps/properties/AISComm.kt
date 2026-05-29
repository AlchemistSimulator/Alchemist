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
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.boundary.gps.loaders.ais.AISShipType
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.Instant

/**
 * Node property exposing AIS data associated with the node.
 *
 * The node is intended to represent the vessel described by the latest AIS payload, while [history] keeps
 * previous payloads when retention is enabled. The AIS vessel identifier is exposed through [vesselId].
 *
 * @param maxSize maximum number of AIS payloads to retain in [history].
 *   Defaults to 100. Use `null` to retain all payloads without any size limit.
 * @param validityWindow optional Alchemist time window for retained payloads in [history].
 */
class AISComm<T> @JvmOverloads constructor(
    node: Node<T>,
    private val maxSize: Int? = 100,
    private val validityWindow: Time? = null,
) : AbstractNodeProperty<T>(node) {
    init {
        require(maxSize == null || maxSize > 0) { "maxSize must be positive or null for unlimited retention" }
        require(validityWindow?.toDouble()?.let { it >= 0.0 } != false) {
            "validityWindow must not be negative"
        }
    }

    private val payloads = ArrayDeque<AISPayload>()

    /**
     * Retained AIS payloads associated with this node, from newest to oldest.
     */
    val history: List<AISPayload>
        get() = payloads.toList()

    /**
     * Latest AIS payload associated with this node.
     */
    val currentData: AISPayload?
        get() = payloads.firstOrNull()

    /**
     * AIS MMSI of the vessel represented by this node.
     */
    val vesselId: Int?
        get() = currentData?.vesselMMSI

    /**
     * Timestamp of the current AIS payload.
     */
    val timestamp: Instant?
        get() = currentData?.timestamp

    /**
     * Current AIS longitude.
     */
    val longitude: Double?
        get() = currentData?.longitude

    /**
     * Current AIS latitude.
     */
    val latitude: Double?
        get() = currentData?.latitude

    /**
     * Current AIS speed over ground, expressed in knots.
     */
    val speedOverGroundKnots: Double?
        get() = currentData?.speedOverGroundKnots

    /**
     * Current AIS speed over ground, expressed in meters per second.
     */
    val speedOverGroundMetersPerSecond: Double?
        get() = currentData?.speedOverGroundMetersPerSecond

    /**
     * Current AIS course over ground, expressed in degrees.
     */
    val courseOverGround: Double?
        get() = currentData?.courseOverGroundDegrees

    /**
     * Current AIS vessel heading, expressed in degrees.
     */
    val heading: Double?
        get() = currentData?.heading

    /**
     * Current raw AIS position accuracy flag.
     */
    val positionAccuracy: Double?
        get() = currentData?.positionAccuracy

    /**
     * Current AIS rate of turn.
     */
    val rateOfTurn: Double?
        get() = currentData?.rateOfTurn

    /**
     * Current raw AIS navigational status.
     */
    val navigationalStatus: Double?
        get() = currentData?.navigationalStatus

    /**
     * Current raw AIS RAIM flag.
     */
    val raim: Double?
        get() = currentData?.raim

    /**
     * Current AIS ship type.
     */
    val shipType: AISShipType?
        get() = currentData?.shipType

    /**
     * Updates this node's AIS data and stores the payload in [history].
     *
     * @param data the AIS payload associated with this node.
     */
    fun update(data: AISPayload) {
        payloads.addFirst(data)
        trim()
    }

    /**
     * Converts an AIS message into payload data and updates this node when the message carries a valid position.
     *
     * @param timestamp the timestamp of the AIS message.
     * @param message the AIS message to use as this node's data.
     */
    fun update(timestamp: Instant, message: AisMessage) {
        AISPayload.from(timestamp, message)?.let(::update)
    }

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = AISComm(node, maxSize, validityWindow).also {
        it.payloads.addAll(payloads)
    }

    private fun trim() {
        maxSize?.let { max ->
            while (payloads.size > max) {
                payloads.removeLast()
            }
        }
        validityWindow?.let { window ->
            payloads
                .maxOfOrNull(AISPayload::timestamp)
                ?.let { newestTimestamp ->
                    payloads.removeAll { payload ->
                        (newestTimestamp - payload.timestamp).toDouble(SECONDS) > window.toDouble()
                    }
                }
        }
    }
}
