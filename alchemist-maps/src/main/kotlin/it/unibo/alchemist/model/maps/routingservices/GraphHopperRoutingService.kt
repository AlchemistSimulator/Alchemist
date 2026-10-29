/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.routingservices

import com.google.common.hash.Hashing
import com.google.common.hash.HashingInputStream
import com.google.common.io.ByteStreams
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.routing.ev.BooleanEncodedValue
import com.graphhopper.routing.util.AccessFilter
import com.graphhopper.routing.weighting.custom.CustomModelParser
import com.graphhopper.util.Constants
import it.unibo.alchemist.boundary.filecache.CacheableDataLoader
import it.unibo.alchemist.boundary.filecache.CachedDirectoryKey
import it.unibo.alchemist.boundary.filecache.FileSystemDirectoryCache
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Route
import it.unibo.alchemist.model.RoutingService
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.maps.routes.GraphHopperRoute
import it.unibo.alchemist.model.routes.PolygonalChain
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

/**
 * [RoutingService] implementation based on GraphHopper.
 *
 * The routing graph of [map] is imported once and stored in a [FileSystemDirectoryCache] rooted in [cacheDirectory],
 * where later instances (also in other simulations) load it from.
 */
class GraphHopperRoutingService
@JvmOverloads
constructor(
    map: URL,
    cacheDirectory: Path = DEFAULT_CACHE_DIRECTORY,
    override val defaultOptions: GraphHopperOptions = Companion.defaultOptions,
) : RoutingService<GeoPosition, GraphHopperOptions> {
    private val graphHopper: GraphHopper = run {
        val graph = RoutingGraph(map)
        val graphDirectory = FileSystemDirectoryCache(RoutingGraph.loader, cacheDirectory)[graph]
        loadNavigationSystem(graphDirectory.resolve(graph.mapName), graphDirectory)
    }

    override fun allowedPointClosestTo(position: GeoPosition, options: GraphHopperOptions): GeoPosition? =
        graphHopper.locationIndex
            .findClosest(
                position.latitude,
                position.longitude,
                AccessFilter.allEdges(
                    graphHopper.encodingManager.getEncodedValue(
                        "${options.vehicleClass}_access",
                        BooleanEncodedValue::class.java,
                    ),
                ),
            ).takeIf { it.isValid }
            ?.snappedPoint
            ?.let { LatLongPosition(it.lat, it.lon) }

    private fun GeoPosition.coerceToMap(): Pair<Double, Double> {
        val bounds = graphHopper.baseGraph.bounds
        return latitude.coerceIn(bounds.minLat..bounds.maxLat) to longitude.coerceIn(bounds.minLon..bounds.maxLon)
    }

    override fun route(from: GeoPosition, to: GeoPosition, options: GraphHopperOptions): Route<GeoPosition> {
        if (from == to) {
            return PolygonalChain(from)
        }
        val naviStart = from.coerceToMap()
        val naviEnd = to.coerceToMap()
        val request: GHRequest =
            GHRequest(naviStart.first, naviStart.second, naviEnd.first, naviEnd.second)
                .setAlgorithm(options.algorithm)
                .setProfile(options.profile.name)
        return GraphHopperRoute(from, to, graphHopper.route(request))
    }

    override fun parseOptions(options: String): GraphHopperOptions {
        TODO("On-the-fly parsing of navigation options is still to be implemented")
    }

    /**
     * Default values for GraphHopper.
     */
    companion object {
        /**
         * See [GraphHopperOptions.defaultOptions].
         */
        val defaultOptions = GraphHopperOptions.defaultOptions

        /**
         * The default root of the cache of the routing graphs.
         */
        val DEFAULT_CACHE_DIRECTORY: Path = FileSystemDirectoryCache.DEFAULT_CACHE_DIRECTORY.resolve("graphhopper")

        private const val HASH_PREFIX_LENGTH = 32

        private val UNSAFE_FILE_NAME_CHARACTERS = Regex("[^A-Za-z0-9._-]")

        private val encodedValues: String = GraphHopperOptions.allCustomModels
            .asSequence()
            .flatMap { model -> CustomModelParser.findVariablesForEncodedValuesString(model, { true }) { it } }
            .distinct()
            .joinToString(separator = ", ")

        private fun newNavigationSystem(mapFile: Path, graphDirectory: Path): GraphHopper = GraphHopper()
            .setOSMFile(mapFile.toAbsolutePath().toString())
            .setElevation(false)
            .setGraphHopperLocation(graphDirectory.toAbsolutePath().toString())
            .setProfiles(GraphHopperOptions.allProfiles)
            .setEncodedValuesString(encodedValues)

        // GraphHopper locks the graph directory while loading: concurrent loads of the same graph would fail.
        // The graph was imported before publishing the cache entry: importing it now would write into a shared entry.
        @Synchronized
        private fun loadNavigationSystem(mapFile: Path, graphDirectory: Path): GraphHopper =
            newNavigationSystem(mapFile, graphDirectory).apply {
                val loaded = runCatching { load() }.getOrElse { error ->
                    throw IllegalStateException(
                        "Unreadable routing graph in $graphDirectory: delete it to import it again",
                        error,
                    )
                }
                check(loaded) { "No routing graph in $graphDirectory: delete it to import it again" }
            }

        /**
         * Reads the content of this URL with [consume], returning its SHA-256 hash.
         */
        private fun URL.sha256(consume: (InputStream) -> Unit): String =
            HashingInputStream(Hashing.sha256(), openStream()).use {
                consume(it)
                it.hash().toString()
            }
    }

    /**
     * The routing graph of [map], identified by the content of [map] and by everything determining the graph
     * GraphHopper imports from it: its version, the profiles, and the encoded values.
     */
    private class RoutingGraph(val map: URL) : CachedDirectoryKey {
        /**
         * The name of the [map] file, as found at the end of the path of its URL, made safe for the file system.
         * It cannot start with `.`, as the cache reserves such names, and it falls back to `map` when empty.
         */
        val mapName: String = map.path
            .substringAfterLast('/')
            .replace(UNSAFE_FILE_NAME_CHARACTERS, "_")
            .trimStart('.')
            .ifEmpty { "map" }

        /**
         * The SHA-256 hash of the content of [map].
         */
        val mapHash: String = map.sha256 { ByteStreams.exhaust(it) }

        override val directoryName: String = run {
            val identity = listOf(
                mapHash,
                Constants.VERSION,
                Constants.getVersions(),
                GraphHopperOptions.allProfiles.joinToString { "${it.name}|${it.version}" },
                encodedValues,
            ).joinToString(separator = "\n")
            val digest = Hashing.sha256().hashString(identity, Charsets.UTF_8).toString()
            "${mapName}_${digest.take(HASH_PREFIX_LENGTH)}"
        }

        companion object {
            /**
             * Copies the map into the cache entry, checking that it is the one the entry is named after,
             * and imports its routing graph there.
             */
            val loader = CacheableDataLoader { graph: RoutingGraph, target: Path ->
                val mapFile = target.resolve(graph.mapName)
                val copiedHash = graph.map.sha256 { Files.copy(it, mapFile) }
                check(copiedHash == graph.mapHash) { "The map at ${graph.map} changed while caching its routing graph" }
                newNavigationSystem(mapFile, target).importAndClose()
            }
        }
    }
}
