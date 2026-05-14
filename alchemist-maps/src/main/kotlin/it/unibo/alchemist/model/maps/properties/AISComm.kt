package it.unibo.alchemist.model.maps.properties

import dk.dma.ais.message.AisMessage
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import java.time.Instant

/**
 * Minimal AIS communication property.
 */
class AISComm<T>(node: Node<T>) : AbstractNodeProperty<T>(node) {
    private val receivedPayloads = mutableListOf<AISPayload>()

    val messages: List<AISPayload>
        get() = receivedPayloads

    val latestMessage: AISPayload?
        get() = receivedPayloads.lastOrNull()

    val speedOverGround: Double?
        get() = latestMessage?.sog

    val courseOverGround: Double?
        get() = latestMessage?.cog

    fun receive(message: AISPayload) {
        receivedPayloads += message
    }

    fun receive(timestamp: Instant, message: AisMessage) {
        AISPayload.from(timestamp, message)?.let(::receive)
    }

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = AISComm(node).also {
        it.receivedPayloads += receivedPayloads
    }
}
