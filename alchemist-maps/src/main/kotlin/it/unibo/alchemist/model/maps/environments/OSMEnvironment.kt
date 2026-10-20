/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.environments

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Route
import it.unibo.alchemist.model.environments.Abstract2DEnvironment
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.maps.routingservices.GraphHopperOptions
import it.unibo.alchemist.model.maps.routingservices.GraphHopperRoutingService
import java.io.File
import java.util.Objects
import java.util.concurrent.TimeUnit
import kotlin.math.max
import org.kaikikm.threadresloader.ResourceLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * An environment backed by an OpenStreetMap extract stored in [file].
 * It encloses the navigation logic, loading the map lazily the first time navigation is required.
 * Routes are cached in memory for performance reasons.
 *
 * @param T concentration type
 * @param incarnation the incarnation to be used.
 * @param file the file path where the map data is stored. Accepts OSM maps of
 *             any format (xml, osm, pbf). The map will be processed, optimized
 *             and stored for future use. If null, navigation is unavailable.
 * @param onStreets if true, the nodes will be placed on the street nearest to the desired position.
 *                  Defaults to [DEFAULT_ON_STREETS] when a [file] is provided, to false otherwise.
 * @param onlyOnStreets if true, the nodes which are too far from a street will be simply discarded.
 *                      If false, they will be placed anyway, in the original position.
 * @param approximation the symbols of IEEE 754-encoded position that may be discarded when comparing two positions,
 *                      allowing quicker retrieval of the route between two positions via cache.
 */
class OSMEnvironment<T> @JvmOverloads constructor(
    incarnation: Incarnation<T, GeoPosition>,
    private val file: String? = null,
    private val onStreets: Boolean = file != null,
    private val onlyOnStreets: Boolean = DEFAULT_FORCE_STREETS,
    private val approximation: Int = DEFAULT_APPROXIMATION,
) : Abstract2DEnvironment<T, GeoPosition>(incarnation),
    MapEnvironment<T, GraphHopperOptions, GraphHopperRoutingService> {
    override val sizeInDistanceUnits: DoubleArray
        get() {
            val (minLongitude, minLatitude) = offset
            val (width, height) = size
            val maxLongitude = minLongitude + width
            val maxLatitude = minLatitude + height
            if (minLatitude.isNaN() || maxLatitude.isNaN() || minLongitude.isNaN() || maxLongitude.isNaN()) {
                return doubleArrayOf(Double.NaN, Double.NaN)
            }
            val minmin = LatLongPosition(minLatitude, minLongitude)
            val minmax = LatLongPosition(minLatitude, maxLongitude)
            val maxmin = LatLongPosition(maxLatitude, minLongitude)
            val maxmax = LatLongPosition(maxLatitude, maxLongitude)
            // Maximum x: maximum distance between the same longitudes; maximum y: between the same latitudes.
            return doubleArrayOf(
                max(minmin.distanceTo(minmax), maxmax.distanceTo(maxmin)),
                max(minmin.distanceTo(maxmin), maxmax.distanceTo(minmax)),
            )
        }

    private val navigator: GraphHopperRoutingService by lazy {
        checkNotNull(file) {
            "Navigation system required, but no map file has been provided." +
                " Consider using BBBike at https://extract.bbbike.org/" +
                " to get a protocolbuffer extract of OpenStreetMap"
        }
        val map = File(file).takeIf(File::exists)?.toURI()?.toURL() ?: ResourceLoader.getResource(file)
        requireNotNull(map) {
            "$file is not a valid file on the file system, nor it can be loaded from the classpath as a Resource"
        }
        GraphHopperRoutingService(map)
    }

    private val routeCache: LoadingCache<CacheEntry, Route<GeoPosition>> = Caffeine.newBuilder()
        .expireAfterAccess(ROUTE_CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS)
        .build { key -> navigator.route(key.start, key.end, key.options) }

    init {
        require(approximation in 0..Long.SIZE_BITS) {
            "The approximation must be between 0 and ${Long.SIZE_BITS}, but it is $approximation"
        }
        if (file == null) {
            logger.warn("No OpenStreetMap extract provided. The navigation system won't be available.")
        }
    }

    override fun computeRoute(p1: GeoPosition, p2: GeoPosition): Route<GeoPosition> =
        computeRoute(p1, p2, GraphHopperRoutingService.defaultOptions)

    override fun computeRoute(from: GeoPosition, to: GeoPosition, options: GraphHopperOptions): Route<GeoPosition> =
        routeCache.get(CacheEntry(options, from, to))

    override fun computeRoute(node: Node<T>, coord: GeoPosition): Route<GeoPosition> =
        computeRoute(node, coord, GraphHopperRoutingService.defaultOptions)

    override fun computeRoute(node: Node<T>, coord: GeoPosition, options: GraphHopperOptions): Route<GeoPosition> =
        computeRoute(currentPositionOf(node), coord, options)

    override fun computeRoute(node: Node<T>, node2: Node<T>): Route<GeoPosition> =
        computeRoute(node, currentPositionOf(node2))

    override fun getRoutingService(): GraphHopperRoutingService = navigator

    override fun makePosition(vararg coordinates: Number): GeoPosition {
        requireBidimensional(coordinates.size)
        return LatLongPosition(coordinates[0].toDouble(), coordinates[1].toDouble())
    }

    override fun makePosition(coordinates: DoubleArray): GeoPosition {
        requireBidimensional(coordinates.size)
        return LatLongPosition(coordinates[0], coordinates[1])
    }

    /**
     * If nodes must be located on streets, queries the navigation engine for a street point.
     * Otherwise, puts them where they are declared.
     */
    override fun computeActualInsertionPosition(node: Node<T>, originalPosition: GeoPosition): GeoPosition = when {
        onStreets -> navigator.allowedPointClosestTo(originalPosition) ?: originalPosition
        else -> originalPosition
    }

    /**
     * There is a single case in which nodes are discarded: if there are no traces
     * for this node and nodes are required to lie on streets, but the navigation
     * engine cannot resolve any such position.
     */
    override fun nodeShouldBeAdded(node: Node<T>, position: GeoPosition): Boolean =
        !onlyOnStreets || navigator.allowedPointClosestTo(position) != null

    private fun requireBidimensional(dimensions: Int) = require(dimensions == 2) {
        "${javaClass.simpleName} only supports bi-dimensional coordinates (latitude, longitude)"
    }

    /**
     * A route request, equal to another when their options match and their endpoints match after [approximation].
     */
    private inner class CacheEntry(val options: GraphHopperOptions, val start: GeoPosition, val end: GeoPosition) {
        private val approximateStart = start.approximate()
        private val approximateEnd = end.approximate()
        private val hash = Objects.hash(options, approximateStart, approximateEnd)

        override fun equals(other: Any?): Boolean = other is OSMEnvironment<*>.CacheEntry &&
            options == other.options &&
            approximateStart == other.approximateStart &&
            approximateEnd == other.approximateEnd

        override fun hashCode(): Int = hash

        private fun Double.approximate(): Double = Double.fromBits(toBits() and (-1L shl approximation))

        private fun GeoPosition.approximate(): GeoPosition = when (approximation) {
            0 -> this
            else -> LatLongPosition(latitude.approximate(), longitude.approximate())
        }
    }

    /**
     * Default values for the [OSMEnvironment] options.
     */
    companion object {
        /**
         * The default value for approximating the position comparison.
         */
        const val DEFAULT_APPROXIMATION = 0

        /**
         * The default value for the discard of nodes too far from streets.
         */
        const val DEFAULT_FORCE_STREETS = false

        private const val ROUTE_CACHE_EXPIRATION_SECONDS = 10L

        private val logger: Logger = LoggerFactory.getLogger(OSMEnvironment::class.java)
    }
}
