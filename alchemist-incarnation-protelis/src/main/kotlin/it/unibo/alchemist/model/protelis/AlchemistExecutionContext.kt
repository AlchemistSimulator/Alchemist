/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.protelis

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.hash.Hashing
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.molecules.SimpleMolecule
import it.unibo.alchemist.model.protelis.properties.ProtelisDevice
import org.apache.commons.math3.random.RandomGenerator
import org.protelis.lang.datatype.DatatypeFactory
import org.protelis.lang.datatype.DeviceUID
import org.protelis.lang.datatype.Field
import org.protelis.lang.datatype.Tuple
import org.protelis.vm.LocalizedDevice
import org.protelis.vm.SpatiallyEmbeddedDevice
import org.protelis.vm.TimeAwareDevice
import org.protelis.vm.impl.AbstractExecutionContext
import java.util.concurrent.TimeUnit
import java.util.function.Function

/**
 * @param <P> position type
*/
class AlchemistExecutionContext<P : Position<P>>
/**
 * @param environmentAccess the simulation [it.unibo.alchemist.model.Environment]
 * @param node the local [it.unibo.alchemist.model.Node]
 * @param protelisDevice the local [it.unibo.alchemist.model.protelis.properties.ProtelisDevice]
 * @param reaction the [it.unibo.alchemist.model.Reaction] hosting the program
 * @param randomGenerator the [org.apache.commons.math3.random.RandomGenerator] for this simulation
 * @param networkManager the [AlchemistNetworkManager] to be used
 */
constructor(
    /**
     * @return experimental access to the simulated environment, for building oracles
     */
    val environmentAccess: Environment<Any, P>,
    private val node: Node<Any>,
    private val protelisDevice: ProtelisDevice<P>,
    private val reaction: Reaction<Any>,
    /**
     * @return the internal [org.apache.commons.math3.random.RandomGenerator]
     */
    val randomGenerator: RandomGenerator,
    networkManager: AlchemistNetworkManager,
) : AbstractExecutionContext<AlchemistExecutionContext<P>>(protelisDevice, networkManager),
    SpatiallyEmbeddedDevice<Double>,
    LocalizedDevice,
    TimeAwareDevice<Number> {
    private val cache =
        CacheBuilder
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(100)
            .build(
                object : CacheLoader<P, Double>() {
                    override fun load(dest: P): Double {
                        if (environmentAccess is MapEnvironment<Any, *, *>) {
                            check(dest is GeoPosition) {
                                "Illegal position type: " + dest::class.qualifiedName + " " + dest
                            }
                            return environmentAccess.computeRoute(node, dest).length()
                        }
                        return devicePosition.distanceTo(dest)
                    }
                },
            )
    private var hash = 0
    private var nbrRangeTimeout = 0.0
    private var precomputedRoutingDistance = Double.NaN

    /**
     * The device position, in form of [Position].
     */
    val devicePosition: P
        get() = environmentAccess.getPosition(node)

    /**
     * @param environment
     * the simulation [Environment]
     * @param localNode
     * the local [Node]
     * @param reaction
     * the [Reaction] hosting the program
     * @param random
     * the [RandomGenerator] for this simulation
     * @param networkManager
     * the [AlchemistNetworkManager] to be used
     */
    constructor(
        environment: Environment<Any, P>,
        localNode: Node<Any>,
        reaction: Reaction<Any>,
        random: RandomGenerator,
        networkManager: AlchemistNetworkManager,
    ) : this(
        environment,
        localNode,
        localNode.asProperty(),
        reaction,
        random,
        networkManager,
    )

    private fun <X> buildFieldWithPosition(processPosition: Function<in P, X>): Field<X> =
        buildField(processPosition, this.devicePosition)

    /**
     * Computes the distance between two nodes, through
     * [Environment.getDistanceBetweenNodes].
     *
     * @param target
     * the target device
     * @return the distance
     */
    fun distanceTo(target: DeviceUID?): Double {
        check(target is ProtelisDevice<*>) {
            "Not a valid " + ProtelisDevice::class.simpleName + ": " + target
        }
        return environmentAccess.getDistanceBetweenNodes(node, target.node)
    }

    /**
     * Computes the distance between two nodes, through
     * [Environment.getDistanceBetweenNodes].
     *
     * @param target
     * the target device
     * @return the distance
     */
    fun distanceTo(target: Int): Double =
        environmentAccess.getDistanceBetweenNodes(node, environmentAccess.getNodeByID(target))

    override fun equals(obj: Any?) = this === obj ||
        obj is AlchemistExecutionContext<*> &&
        (
            node == obj.node &&
                this.environmentAccess == obj.environmentAccess &&
                reaction == obj.reaction &&
                randomGenerator == obj.randomGenerator
            )

    override fun getCoordinates(): Tuple = DatatypeFactory.createTuple(this.devicePosition.coordinates.toList())

    override fun getCurrentTime() = reaction.tau.toDouble()

    @SuppressFBWarnings(value = ["EI_EXPOSE_REP"], justification = INTENTIONAL)
    override fun getDeviceUID(): DeviceUID = protelisDevice

    override fun hashCode(): Int {
        if (hash == 0) {
            hash =
                Hashing
                    .murmur3_32_fixed()
                    .newHasher()
                    .putInt(node.id)
                    .putInt(environmentAccess.hashCode())
                    .putInt(reaction.hashCode())
                    .hash()
                    .asInt()
        }
        return hash
    }

    override fun instance(): AlchemistExecutionContext<P> = AlchemistExecutionContext<P>(
        this.environmentAccess,
        node,
        reaction,
        randomGenerator,
        networkManager as AlchemistNetworkManager,
    )

    /**
     * @return The same behavior of MIT Proto's nbrdelay (forward view).
     */
    override fun nbrDelay(): Field<Number> = buildField<Number, Number>({ it }, deltaTime)

    override fun nbrLag(): Field<Number> = buildField<Double, Number>(
        { time: Double -> getCurrentTime().toDouble() - time },
        getCurrentTime().toDouble(),
    )

    override fun nbrRange(): Field<Double> {
        val useRoutesAsDistances =
            this.environmentAccess is MapEnvironment<*, *, *> && node.contains(USE_ROUTES_AS_DISTANCES)
        return buildFieldWithPosition<Double> { p: P ->
            if (useRoutesAsDistances) {
                check(p is GeoPosition) {
                    "Illegal position type: " + p::class.simpleName + " " + p
                }
                if (node.contains(APPROXIMATE_NBR_RANGE)) {
                    val tolerance = node.getConcentration(APPROXIMATE_NBR_RANGE)
                    check(tolerance is Double) {
                        "APPROXIMATE_NBR_RANGE should be associated with a double concentration"
                    }
                    val currTime = environmentAccess.simulation.getTime().toDouble()
                    if (currTime > nbrRangeTimeout) {
                        nbrRangeTimeout = currTime + tolerance
                        precomputedRoutingDistance = routingDistance(p)
                    }
                    check(!precomputedRoutingDistance.isNaN())
                }
                return@buildFieldWithPosition routingDistance(p)
            }
            this.devicePosition.distanceTo(p)
        }
    }

    override fun nbrVector(): Field<Tuple> = buildFieldWithPosition { p: P ->
        val diff = this.devicePosition.minus(p.coordinates)
        check(diff is Position2D<*>) {
            "Protelis support for 3D environments not ready yet: $diff"
        }
        DatatypeFactory.createTuple(diff.x, diff.y)
    }

    override fun nextRandomDouble(): Double = randomGenerator.nextDouble()

    /**
     * Computes the distance along a map. Requires a [MapEnvironment].
     *
     * @param dest
     * the destination, in the form of a destination node
     * @return the distance on a map
     */
    fun routingDistance(dest: Node<Any>): Double = routingDistance(environmentAccess.getPosition(dest) as GeoPosition)

    /**
     * Computes the distance along a map. Requires a [MapEnvironment].
     *
     * @param dest
     * the destination, in form of [Node] ID.
     * Non-integer numbers will be cast to integers.
     * @return the distance on a map
     */
    fun routingDistance(dest: Number): Double = routingDistance(environmentAccess.getNodeByID(dest.toInt()))

    /**
     * Computes the distance along a map. Requires a [MapEnvironment].
     *
     * @param dest
     * the destination
     * @return the distance on a map
     */
    @Suppress("UNCHECKED_CAST")
    fun routingDistance(dest: GeoPosition): Double = cache.get(dest as P)

    /**
     * Computes the distance along a map. Requires a [MapEnvironment].
     *
     * @param dest
     * the destination, as a [Tuple] of two values: [latitude,
     * longitude]
     * @return the distance on a map
     */
    fun routingDistance(dest: Tuple): Double {
        require(dest.size() == 2) {
            "$dest is not a coordinate I can understand."
        }
        val (latitude, longitude) = dest.toList()
        require(latitude is Number) {
            "$latitude (${latitude::class.simpleName}) in $dest is not a valid coordinate."
        }
        require(longitude is Number) {
            "$longitude (${longitude::class.simpleName}) in $dest is not a valid coordinate."
        }
        return routingDistance(LatLongPosition(latitude, longitude))
    }

    /**
     * Internal constants.
     */
    companion object {
        /**
         * Put this [it.unibo.alchemist.model.Molecule] inside nodes that should compute distances using routes.
         * It only makes sense in case the environment is a [MapEnvironment]
         */
        val USE_ROUTES_AS_DISTANCES: Molecule = SimpleMolecule("ROUTES_AS_DISTANCE")

        /**
         * Put this [Molecule] inside nodes that should compute distances using routes approximating them.
         * It only makes sense in case the environment is a [MapEnvironment]
         */
        val APPROXIMATE_NBR_RANGE: Molecule = SimpleMolecule("APPROXIMATE_NBR_RANGE")

        private const val INTENTIONAL = "This is intentional"
    }
}
