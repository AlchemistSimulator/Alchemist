/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime

/**
 * This class models a distribution that follows the packet arrival times as described in
 * [EdgeCloudSim](https://ieeexplore.ieee.org/document/7946405).
 * The delay produced depends on a constant propagation delay plus the packet size divided by the bandwidth.
 *
 * @param T the type of the environment. In case propagationDelay, packetSize or bandwidth are not parsable to [Double],
 * then they will be used as identifiers for properties to be obtained through [the incarnation][Incarnation]
 *
 * @param propagationDelayMolecule The propagation delay molecule. If the string is parsable as [Double],
 * then it will get used as a constant delay. Otherwise, the String will be used within [Incarnation.getProperty]
 *
 */
class SimpleNetworkArrivals<T> private constructor(
    /** The incarnation used to resolve properties from nodes. */
    val incarnation: Incarnation<T, *>,
    /** The environment containing the network topology. */
    val environment: Environment<T, *>,
    /** The node where this time distribution is applied. */
    val node: Node<T>,
    private val constantPropagationDelay: Double? = null,
    private val propagationDelayMolecule: Molecule? = null,
    private val propagationDelayProperty: String? = null,
    private val constantPacketSize: Double? = null,
    private val packetSizeMolecule: Molecule? = null,
    private val packetSizeProperty: String? = null,
    private val constantBandwidth: Double? = null,
    private val bandwidthMolecule: Molecule? = null,
    private val bandwidthProperty: String? = null,
    private val accessPointIdentificator: Molecule? = null,
    startTime: Time = Time.ZERO,
) : AbstractDistribution<T>(startTime) {

    @JvmOverloads
    constructor(
        incarnation: Incarnation<T, *>,
        node: Node<T>,
        environment: Environment<T, *>,
        propagationDelay: Double,
        packetSize: Double,
        bandwidth: Double,
        accessPointIdentificator: Molecule? = null,
    ) : this(
        incarnation,
        environment,
        node,
        constantPropagationDelay = propagationDelay,
        constantPacketSize = packetSize,
        constantBandwidth = bandwidth,
        accessPointIdentificator = accessPointIdentificator,
    )

    @JvmOverloads
    constructor(
        incarnation: Incarnation<T, *>,
        environment: Environment<T, *>,
        node: Node<T>,
        propagationDelay: Double,
        packetSize: Double,
        bandwidthMolecule: Molecule,
        bandwidthProperty: String,
        accessPointIdentificator: Molecule? = null,
    ) : this(
        incarnation,
        environment,
        node,
        constantPropagationDelay = propagationDelay,
        constantPacketSize = packetSize,
        bandwidthMolecule = bandwidthMolecule,
        bandwidthProperty = bandwidthProperty,
        accessPointIdentificator = accessPointIdentificator.isMeaningful,
    )

    @JvmOverloads
    constructor(
        incarnation: Incarnation<T, *>,
        environment: Environment<T, *>,
        node: Node<T>,
        propagationDelay: Double,
        packetSizeMolecule: Molecule,
        packetSizeProperty: String,
        bandwidthMolecule: Molecule,
        bandwidthProperty: String,
        accessPointIdentificator: Molecule? = null,
    ) : this(
        incarnation, environment, node,
        constantPropagationDelay = propagationDelay,
        packetSizeMolecule = packetSizeMolecule,
        packetSizeProperty = packetSizeProperty,
        bandwidthMolecule = bandwidthMolecule,
        bandwidthProperty = bandwidthProperty,
        accessPointIdentificator = accessPointIdentificator.isMeaningful,
    )

    @JvmOverloads
    constructor(
        incarnation: Incarnation<T, *>,
        environment: Environment<T, *>,
        node: Node<T>,
        propagationDelayMolecule: Molecule,
        propagationDelayProperty: String,
        packetSizeMolecule: Molecule,
        packetSizeProperty: String,
        bandwidthMolecule: Molecule,
        bandwidthProperty: String,
        accessPointIdentificator: Molecule? = null,
    ) : this(
        incarnation, environment, node,
        constantPropagationDelay = null,
        propagationDelayMolecule = propagationDelayMolecule,
        propagationDelayProperty = propagationDelayProperty,
        packetSizeMolecule = packetSizeMolecule,
        packetSizeProperty = packetSizeProperty,
        bandwidthMolecule = bandwidthMolecule,
        bandwidthProperty = bandwidthProperty,
        accessPointIdentificator = accessPointIdentificator.isMeaningful,
    )

    private var time: Time = startTime

    private val myNeighborhood
        get() = node.neighborhood

    private val Node<T>.isAccessPoint
        get() = accessPointIdentificator?.let { this.contains(it) } ?: false

    /** Gets the neighbors of a node as a collection. */
    val Node<T>.neighborhood: Collection<Node<T>>
        get() = environment.getNeighborhood(this).neighbors

    /** Computes the effective bandwidth considering access point load balancing. */
    val bandwidth: Double
        get() = (
            constantBandwidth ?: incarnation.getProperty(node, bandwidthMolecule, bandwidthProperty)
            ).let { bw ->
            accessPointIdentificator?.let { id ->
                if (node.isAccessPoint || myNeighborhood.isEmpty()) {
                    bw / myNeighborhood.size.coerceAtLeast(1)
                } else {
                    val accesspoints = myNeighborhood.filter { it.isAccessPoint }
                    when (accesspoints.size) {
                        0 -> bw
                        1 -> bw / accesspoints.first().neighborhood.size
                        else -> error(
                            "node ${node.id} is connected to multiple access points: $accesspoints",
                        )
                    }
                }
            } ?: bw
        }

    /**
     * Computes the packet size from constants or properties.
     * Defaults to 1.0 if not specified or invalid.
     */
    val packetSize: Double
        get() = constantPacketSize
            ?: incarnation.getProperty(node, packetSizeMolecule, packetSizeProperty).takeIf { it.isFinite() && it >= 0 }
            ?: 1.0

    /** Computes the propagation delay from constants or properties. */
    val propagationDelay: Double
        get() = constantPropagationDelay
            ?: incarnation.getProperty(node, propagationDelayMolecule, propagationDelayProperty)

    override fun updateStatus(currentTime: Time, executed: Boolean, rate: Double, environment: Environment<T, *>) {
        /*
         * To be revised once we have a better infrastructure of events and time distributions
         */
        if (rate == 0.0) {
            time = Time.INFINITY
        } else if (time.isInfinite) {
            time = currentTime + propagationDelay + packetSize / bandwidth
        }
        setNextOccurrence(time)
    }

    override fun cloneOnNewNode(destination: Node<T>, currentTime: Time): SimpleNetworkArrivals<T> =
        SimpleNetworkArrivals(
            incarnation = incarnation,
            environment = environment,
            node = destination,
            constantPropagationDelay = constantPropagationDelay,
            propagationDelayMolecule = propagationDelayMolecule,
            propagationDelayProperty = propagationDelayProperty,
            constantPacketSize = constantPacketSize,
            packetSizeMolecule = packetSizeMolecule,
            packetSizeProperty = packetSizeProperty,
            constantBandwidth = constantBandwidth,
            bandwidthMolecule = bandwidthMolecule,
            bandwidthProperty = bandwidthProperty,
            accessPointIdentificator = accessPointIdentificator,
            startTime = currentTime,
        )

    override fun getRate(): Double = 1 / (propagationDelay + packetSize / bandwidth)

    private companion object {
        private val Molecule?.isMeaningful: Molecule?
            get() = this?.takeUnless { name.isNullOrBlank() }

        private operator fun Time.plus(other: Double) = DoubleTime(toDouble() + other)
    }
}
