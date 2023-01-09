/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.routingservices

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import com.google.common.hash.Hashing
import com.graphhopper.GHRequest
import com.graphhopper.GraphHopper
import com.graphhopper.routing.ev.VehicleAccess
import com.graphhopper.routing.util.AccessFilter
import com.graphhopper.routing.util.DefaultVehicleEncodedValuesFactory
import com.graphhopper.routing.util.EncodingManager
import it.unibo.alchemist.model.implementations.positions.LatLongPosition
import it.unibo.alchemist.model.implementations.routes.GraphHopperRoute
import it.unibo.alchemist.model.implementations.routes.PolygonalChain
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.model.interfaces.Route
import it.unibo.alchemist.model.interfaces.RoutingService
import net.harawata.appdirs.AppDirsFactory
import org.apache.commons.codec.binary.Base32
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Semaphore
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory

/**
 * [RoutingService] implementation based on GraphHopper.
 */
class GraphHopperRoutingService @JvmOverloads constructor(
    map: URL,
    workingDirectory: File = defaultWorkingDirectory(map.openStream()),
    override val defaultOptions: GraphHopperOptions = Companion.defaultOptions,
) : RoutingService<GeoPosition, GraphHopperOptions> {

    private val graphHopper: GraphHopper
    private val accessFilters: LoadingCache<GraphHopperOptions, AccessFilter>

    init {
        val mapName = map.toExternalForm().split('/').last().takeWhile { it != '?' }
        val mapFile = File(workingDirectory, mapName)
        lockfileLock.acquireUninterruptibly()
        try {
            RandomAccessFile(File(workingDirectory, ".lock"), "rw").use { fileAccess ->
                fileAccess.channel.lock().use {
                    if (!mapFile.exists()) {
                        Files.copy(map.openStream(), mapFile.toPath())
                    }
                }
                graphHopper = runCatching { initNavigationSystem(mapFile, workingDirectory) }
                    .recoverCatching { ex ->
                        logger.warn(
                            "Could not initialize with $mapFile (version conflict?): erasing cache and retrying",
                            ex
                        )
                        val corruptedContent = workingDirectory.listFiles()?.filterNot { it == mapFile }.orEmpty()
                        corruptedContent.forEach {
                            require(it.deleteRecursively()) {
                                "Could not delete $$it. Something nasty is going on with your file system"
                            }
                        }
                        workingDirectory.mkdirs()
                        initNavigationSystem(mapFile, workingDirectory)
                    }
                    .getOrThrow()
            }
        } finally {
            lockfileLock.release()
        }
        accessFilters = Caffeine.newBuilder().build {
            AccessFilter.allEdges(
                EncodingManager
                    .create(vehicleEncoder, it.profile.vehicle)
                    .getBooleanEncodedValue(VehicleAccess.key(it.profile.vehicle))
            )
        }
    }

    override fun allowedPointClosestTo(position: GeoPosition, options: GraphHopperOptions): GeoPosition? {
        return graphHopper.locationIndex
            .findClosest(position.latitude, position.longitude, accessFilters[options])
            .takeIf { it.isValid }
            ?.snappedPoint
            ?.let { LatLongPosition(it.lat, it.lon) }
    }

    private fun GeoPosition.coerceToMap(): Pair<Double, Double> {
        val bounds = graphHopper.baseGraph.bounds
        return latitude.coerceIn(bounds.minLat..bounds.maxLat) to longitude.coerceIn(bounds.minLon..bounds.maxLon)
    }

    override fun route(
        from: GeoPosition,
        to: GeoPosition,
        options: GraphHopperOptions
    ): Route<GeoPosition> {
        if (from == to) {
            return PolygonalChain(from)
        }
        val naviStart = from.coerceToMap()
        val naviEnd = to.coerceToMap()
        val request: GHRequest = GHRequest(naviStart.first, naviStart.second, naviEnd.first, naviEnd.second)
            .setAlgorithm(options.algorithm)
            .setProfile(options.profile.name)
        return GraphHopperRoute(from, to, graphHopper.route(request))
    }

    override fun parseOptions(options: String): GraphHopperOptions {
        TODO("On-the-fly parsing of navigation options is still to be implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GraphHopperRoutingService::class.java)
        private val lockfileLock = Semaphore(1)
        private val vehicleEncoder = DefaultVehicleEncodedValuesFactory()

        /**
         * See [GraphHopperOptions.defaultOptions].
         */
        val defaultOptions = GraphHopperOptions.defaultOptions

        @Synchronized
        private fun defaultWorkingDirectory(map: InputStream): File {
            val code = map.nameFromHash()
            val appDirs = AppDirsFactory.getInstance()
            val possibleLocations: Sequence<(String, String, String) -> String> = sequenceOf(
                appDirs::getUserCacheDir,
                appDirs::getUserDataDir,
                appDirs::getUserConfigDir,
                { app, version, _ -> createTempDirectory("$app-$version").absolutePathString() },
                { app, version, _ -> File(Paths.get("").toFile(), "$app-$version").apply { mkdirs() }.absolutePath },
            )
            return possibleLocations.map { File(it("alchemist", "map-$code$", "it.unibo")) }.firstOrNull { folder ->
                if (folder.exists()) {
                    (folder.isDirectory && folder.canWrite())
                        .also { if (!it) logger.warn("{} is not writeable", folder) }
                } else {
                    runCatching { folder.mkdirs() }
                        .onFailure { logger.warn("Directory structure $folder could not be created", it) }
                        .isSuccess
                }
            } ?: error("No writeable path was found.")
        }

        @Synchronized
        private fun initNavigationSystem(
            mapFile: File,
            internalWorkdir: File,
        ): GraphHopper = GraphHopper()
            .setOSMFile(mapFile.absolutePath)
            .setElevation(false)
            .setGraphHopperLocation(internalWorkdir.absolutePath)
            .setProfiles(GraphHopperOptions.allProfiles)
            .importOrLoad()

        private fun InputStream.nameFromHash(): String =
            Base32().encodeAsString(Hashing.sha256().hashBytes(readAllBytes()).asBytes()).filter { it != '=' }
    }
}
