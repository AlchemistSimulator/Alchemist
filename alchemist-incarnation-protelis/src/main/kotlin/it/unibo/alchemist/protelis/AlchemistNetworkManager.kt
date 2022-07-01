/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.protelis

import com.google.common.collect.ImmutableMap
import it.unibo.alchemist.model.implementations.actions.RunProtelisProgram
import it.unibo.alchemist.model.implementations.properties.ProtelisDevice
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.interfaces.Reaction
import org.apache.commons.math3.distribution.RealDistribution
import org.protelis.lang.datatype.DeviceUID
import org.protelis.vm.CodePath
import org.protelis.vm.NetworkManager
import java.io.Serializable
import java.util.Collections
import java.util.Objects

/**
 * Emulates a [NetworkManager]. This particular network manager does not
 * send messages instantly. Instead, it records the last message to send, and
 * only when [simulateMessageArrival] is called the transfer is
 * actually performed.
 */
class AlchemistNetworkManager @JvmOverloads constructor(
    /**
     * This reaction stores the time at which the neighbor state is read.
     */
    val event: Reaction<Any>,
    /**
     * The [ProtelisDevice] required to run Protelis.
     */
    val device: ProtelisDevice<*> = event.node.asProperty(),
    /**
     * The action this network manager is associated with.
     */
    val program: RunProtelisProgram<*>,
    /**
     * the message retention time, or NaN if all the messages get
     * discarded as soon as a computation cycle is concluded.
     */
    val retentionTime: Double = Double.NaN,
    /**
     * the distribution connecting the distance to the packet loss.
     */
    val distanceLossDistribution: RealDistribution? = null,
) : NetworkManager, Serializable {

    private val environment: Environment<Any, *> = Objects.requireNonNull(program.environment)
    private val messages: MutableMap<DeviceUID, MessageInfo> = LinkedHashMap()
    private var toBeSent: Map<CodePath, Any> = emptyMap()
    private var neighborState = ImmutableMap.of<DeviceUID, Map<CodePath, Any>>()
    private var timeAtLastValidityCheck = Double.NEGATIVE_INFINITY

    init {
        require(retentionTime.isNaN() || retentionTime >= 0) { "The retention time can't be negative." }
    }

    override fun getNeighborState(): ImmutableMap<DeviceUID, Map<CodePath, Any>> {
        val currentTime = event.tau.toDouble()
        /*
         * If no time has passed, the last result is still valid, otherwise needs to be recomputed
         */
        if (timeAtLastValidityCheck != currentTime) {
            neighborState = if (messages.isEmpty()) {
                ImmutableMap.of()
            } else {
                /*
                 * If retentionTime is a number, use it. Otherwise clean messages of lost neighbors
                 */
                val stateBuilder = ImmutableMap.builder<DeviceUID, Map<CodePath, Any>>()
                val messagesIterator = messages.values.iterator()
                val retainsNeighbors = retentionTime.isNaN()
                val neighbors: Set<DeviceUID> = emptySet<DeviceUID>().takeUnless { retainsNeighbors }
                    ?: environment.getNeighborhood(device.node)
                        .neighbors
                        .mapNotNull { it.asPropertyOrNull<Any, ProtelisDevice<*>>() }
                        .toSet()
                while (messagesIterator.hasNext()) {
                    val message = messagesIterator.next()
                    if (retainsNeighbors && message.source in neighbors || currentTime - message.time < retentionTime) {
                        stateBuilder.put(message.source, message.payload)
                    } else {
                        // Removes from this.messages as well
                        messagesIterator.remove()
                    }
                }
                stateBuilder.build()
            }
            timeAtLastValidityCheck = currentTime
        }
        return neighborState
    }

    private fun receiveMessage(msg: MessageInfo) {
        messages[msg.source] = msg
    }

    override fun shareState(toSend: Map<CodePath, Any>) {
        toBeSent = Collections.unmodifiableMap(toSend)
    }

    /**
     * Simulates the arrival of the message to other nodes.
     *
     * @param currentTime
     * the current simulation time (used to understand when a message
     * should get dropped).
     */
    fun simulateMessageArrival(currentTime: Double) {
        if (toBeSent.isNotEmpty()) {
            val msg = MessageInfo(currentTime, device, toBeSent)
            environment.getNeighborhood(device.node)
                .mapNotNull { it.asPropertyOrNull<Any, ProtelisDevice<*>>() }
                .forEach { neighborDevice ->
                    val destination = neighborDevice.getNetworkManager(program)
                    var packetArrives = true
                    if (distanceLossDistribution != null) {
                        val distance = environment.getDistanceBetweenNodes(device.node, neighborDevice.node)
                        val random = program.randomGenerator.nextDouble()
                        packetArrives = random > distanceLossDistribution.cumulativeProbability(distance)
                    }
                    if (packetArrives) {
                        /*
                         * The node is running the program, and the loss model actually makes the packet arrive.
                         * Otherwise, the message is discarded
                         */
                        destination.receiveMessage(msg)
                    }
                }
            toBeSent = emptyMap()
        }
    }

    private data class MessageInfo(
        val time: Double,
        val source: DeviceUID,
        val payload: Map<CodePath, Any>
    ) : Serializable {
        companion object {
            private const val serialVersionUID = 2L
        }
    }

    companion object {
        private const val serialVersionUID = 2L
    }
}
