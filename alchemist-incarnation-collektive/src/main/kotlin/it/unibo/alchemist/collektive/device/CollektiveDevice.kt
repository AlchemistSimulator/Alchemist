/*
 * Copyright (c) 2025, Danilo Pianini, Nicolas Farabegoli, Elisa Tronetti,
 * and all authors listed in the `build.gradle.kts` and the generated `pom.xml` file.
 *
 * This file is part of Collektive, and is distributed under the terms of the Apache License 2.0,
 * as described in the LICENSE file in this project's repository's top directory.
 */

package it.unibo.alchemist.collektive.device

import it.unibo.alchemist.model.Capability
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.collektive.aggregate.Field
import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.DataSharingMethod
import it.unibo.collektive.aggregate.api.neighboring
import it.unibo.collektive.alchemist.device.sensors.EnvironmentVariables
import it.unibo.collektive.networking.Mailbox
import it.unibo.collektive.networking.Message
import it.unibo.collektive.networking.NeighborsData
import it.unibo.collektive.networking.NoNeighborsData
import it.unibo.collektive.networking.OutboundEnvelope
import it.unibo.collektive.path.Path
import org.apache.commons.math3.random.RandomGenerator

/**
 * Representation of a Collektive device (as a [node]) in an Alchemist [environment] with a [randomGenerator].
 * [P] is the position type,
 * [retainMessagesFor] is the time for which messages are retained.
 */
class CollektiveDevice<P>(
    val randomGenerator: RandomGenerator,
    val environment: Environment<Any?, P>,
    override val node: Node<Any?>,
    private val retainMessagesFor: Time? = null,
) : Capability<Any?>,
    Mailbox<Int>,
    EnvironmentVariables where P : Position<P> {

    private data class TimedMessage(val receivedAt: Time, val payload: Message<Int, *>)

    override val inMemory: Boolean = true

    /**
     * The current time.
     */
    val currentTime: Time
        get() = environment.simulationOrNull?.currentTime ?: Time.ZERO

    /**
     * The ID of the node (alias of [localId]).
     */
    val id = node.id

    /**
     * The ID of the node (alias of [id]).
     */
    val localId = node.id

    private val validMessages: MutableMap<Int, TimedMessage> = mutableMapOf()

    private fun receiveMessage(time: Time, message: Message<Int, *>) {
        validMessages += message.senderId to TimedMessage(time, message)
    }

    /**
     * Returns the distances to the neighboring nodes.
     */
    fun <ID : Any> Aggregate<ID>.distances(): Field<ID, Double> =
        environment.currentPositionOf(node).let { nodePosition ->
            neighboring(nodePosition.coordinates).mapValues { position ->
                nodePosition.distanceTo(environment.makePosition(position))
            }
        }

    override fun cloneOnNewNode(node: Node<Any?>): Capability<Any?> =
        CollektiveDevice(randomGenerator, environment, node, retainMessagesFor)

    override fun deliverableFor(outboundMessage: OutboundEnvelope<Int>) {
        if (outboundMessage.isNotEmpty()) {
            val neighborsNodes = environment.neighborhoodOf(node).current
            if (!neighborsNodes.isEmpty) {
                val neighborhood =
                    neighborsNodes.neighbors.mapNotNull { node ->
                        @Suppress("UNCHECKED_CAST")
                        node.properties.firstOrNull { it is CollektiveDevice<*> } as? CollektiveDevice<P>
                    }
                neighborhood.forEach { neighbor ->
                    neighbor.deliverableReceived(outboundMessage.prepareMessageFor(neighbor.id))
                }
            }
        }
    }

    override fun deliverableReceived(message: Message<Int, *>) {
        receiveMessage(currentTime, message)
    }

    override fun currentInbound(): NeighborsData<Int> {
        if (validMessages.isEmpty()) {
            return NoNeighborsData()
        }
        val messages: Map<Int, Message<Int, *>> =
            when {
                retainMessagesFor == null -> validMessages.mapValues { it.value.payload }.also { validMessages.clear() }
                else -> {
                    validMessages.values.retainAll { it.receivedAt + retainMessagesFor >= currentTime }
                    validMessages.mapValues { it.value.payload }
                }
            }
        return object : NeighborsData<Int> {
            override val neighbors: Set<Int> get() = messages.keys

            @Suppress("UNCHECKED_CAST")
            override fun <Value> dataAt(path: Path, dataSharingMethod: DataSharingMethod<Value>): Map<Int, Value> =
                // Messages need to be filtered by the path to allow for null values
                messages.filterValues { message -> message.sharedData.containsKey(path) }
                    .mapValues { (_, message) -> message.sharedData[path] as Value }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(name: String): T = node[SimpleMolecule(name)].getOrNull() as T

    override fun <T> getOrNull(name: String): T? = when {
        isDefined(name) -> get(name)
        else -> null
    }

    override fun <T> getOrDefault(name: String, default: T): T = getOrNull(name) ?: default

    override fun isDefined(name: String): Boolean = node.contains(SimpleMolecule(name))

    override fun <T> set(name: String, value: T): T = value.also { node[SimpleMolecule(name)] = it }
}
