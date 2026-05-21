/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.properties

import dk.dma.ais.message.AisMessage
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Minimal AIS communication property.
 *
 * @param maxSize maximum number of payloads to retain.
 * @param validityWindow optional time window for retained payloads.
 */
class AISComm<T>(
    node: Node<T>,
    private val maxSize: Int = Int.MAX_VALUE,
    private val validityWindow: Duration? = null,
) : AbstractNodeProperty<T>(node) {
    init {
        require(maxSize > 0) { "maxSize must be positive" }
        require(validityWindow?.isNegative() != true) { "validityWindow must not be negative" }
    }

    private val receivedPayloads = ArrayDeque<AISPayload>()

    /**
     * List of all retained AIS payloads, from newest to oldest receipt.
     */
    val messages: List<AISPayload>
        get() = receivedPayloads.toList()

    /**
     * The most recently received AIS payload.
     */
    val latestMessage: AISPayload?
        get() = receivedPayloads.firstOrNull()

    /**
     * Speed over ground (in knots) from the latest AIS message.
     */
    val speedOverGroundKnots: Double?
        get() = latestMessage?.speedOverGroundKnots

    /**
     * Speed over ground (in m/s) from the latest AIS message.
     */
    val speedOverGroundMetersPerSecond: Double?
        get() = latestMessage?.speedOverGroundMetersPerSecond

    /**
     * Course over ground (in degrees) from the latest AIS message.
     */
    val courseOverGround: Double?
        get() = latestMessage?.courseOverGround

    /**
     * Receives an AIS payload and adds it to the list of received messages.
     *
     * @param message the AIS payload to receive.
     */
    fun receive(message: AISPayload) {
        receivedPayloads.addFirst(message)
        trim()
    }

    /**
     * Receives an AIS message with a timestamp, converts it to an AIS payload
     * and adds it to the list of received messages.
     *
     * @param timestamp the timestamp of the message.
     * @param message the AIS message to receive.
     */
    fun receive(timestamp: Instant, message: AisMessage) {
        AISPayload.from(timestamp, message)?.let(::receive)
    }

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = AISComm(node, maxSize, validityWindow).also {
        it.receivedPayloads.addAll(receivedPayloads)
    }

    private fun trim() {
        while (receivedPayloads.size > maxSize) {
            receivedPayloads.removeLast()
        }
        validityWindow?.let { window ->
            receivedPayloads
                .maxOfOrNull(AISPayload::timestamp)
                ?.minus(window)
                ?.let { oldestRetainedTimestamp ->
                    receivedPayloads.removeAll { payload -> payload.timestamp < oldestRetainedTimestamp }
                }
        }
    }
}
