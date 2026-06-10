/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.properties

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.boundary.gps.loaders.ais.AISNavigationStatus
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.boundary.gps.loaders.ais.AISShipType
import it.unibo.alchemist.boundary.gps.loaders.ais.AISTrace
import it.unibo.alchemist.boundary.gps.loaders.ais.AISTraceLoader
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import java.io.Serial
import java.util.Objects
import java.util.concurrent.TimeUnit
import kotlin.time.Instant

/**
 * Node property exposing current AIS data associated with the node.
 *
 * AIS metadata is loaded from [path] and one MMSI trace is allocated to each vessel property in the same environment.
 * The node position remains managed by the environment and by movement actions.
 */
class AISVessel<T>(
    private val environment: MapEnvironment<T, *, *>,
    node: Node<T>,
    private val path: String,
    @Suppress("UNUSED_PARAMETER") private val cycle: Boolean = false,
    private val normalizer: String = "AlignToSimulationTime",
    private vararg val normalizerArgs: Any?,
) : AbstractNodeProperty<T>(node) {
    private val trace: AISTrace by lazy { traceFor(environment, path, normalizer, *normalizerArgs) }

    /**
     * Current AIS payload for the simulation time.
     */
    val currentPayload: AISPayload
        get() = trace.payloadAt(environment.simulationOrNull?.time ?: error("AISVessel requires a simulation time"))

    /**
     * AIS MMSI of the vessel represented by this node.
     */
    val vesselId: Int
        get() = trace.vesselMMSI

    /**
     * Timestamp of the current AIS payload.
     */
    val timestamp: Instant
        get() = currentPayload.timestamp

    /** Current AIS latitude, if present in the AIS stream. Environment position remains authoritative. */
    val latitude: Double?
        get() = currentPayload.latitude

    /** Current AIS longitude, if present in the AIS stream. Environment position remains authoritative. */
    val longitude: Double?
        get() = currentPayload.longitude

    /** Current AIS speed over ground, expressed in knots. */
    val speedOverGroundKnots: Double?
        get() = currentPayload.speedOverGroundKnots

    /** Current AIS speed over ground, expressed in meters per second. */
    val speedOverGroundMetersPerSecond: Double?
        get() = currentPayload.speedOverGroundMetersPerSecond

    /** Current AIS course over ground, expressed in degrees. */
    val courseOverGroundDegrees: Double?
        get() = currentPayload.courseOverGroundDegrees

    /** Current AIS course over ground, expressed in radians. */
    val courseOverGroundRadians: Double?
        get() = currentPayload.courseOverGroundRadians

    /** Current AIS vessel heading, expressed in degrees. */
    val headingDegrees: Double?
        get() = currentPayload.headingDegrees

    /** Current AIS vessel heading, expressed in radians. */
    val headingRadians: Double?
        get() = currentPayload.headingRadians

    /** Current raw AIS position accuracy flag. */
    val positionAccuracy: Double?
        get() = currentPayload.positionAccuracy

    /** Current AIS rate of turn. */
    val rateOfTurn: Double?
        get() = currentPayload.rateOfTurn

    /** Current AIS navigational status. */
    val navigationalStatus: AISNavigationStatus?
        get() = currentPayload.navigationalStatus

    /** Whether the current AIS data reports receiver autonomous integrity monitoring. */
    val isEquippedWithRAIM: Boolean?
        get() = currentPayload.isEquippedWithRAIM

    /** Current AIS ship type. */
    val shipType: AISShipType?
        get() = currentPayload.shipType

    /** Current AIS callsign. */
    val callsign: String?
        get() = currentPayload.callsign

    /** Current AIS vessel name. */
    val vesselName: String?
        get() = currentPayload.vesselName

    /** Current AIS dimension to bow, in meters. */
    val dimensionToBow: Int?
        get() = currentPayload.dimensionToBow

    /** Current AIS dimension to stern, in meters. */
    val dimensionToStern: Int?
        get() = currentPayload.dimensionToStern

    /** Current AIS dimension to port, in meters. */
    val dimensionToPort: Int?
        get() = currentPayload.dimensionToPort

    /** Current AIS dimension to starboard, in meters. */
    val dimensionToStarboard: Int?
        get() = currentPayload.dimensionToStarboard

    /** Current AIS IMO number. */
    val imoNumber: Long?
        get() = currentPayload.imoNumber

    /** Current AIS positioning device code. */
    val positioningDevice: Int?
        get() = currentPayload.positioningDevice

    /** Current AIS estimated time of arrival. */
    val eta: Instant?
        get() = currentPayload.eta

    /** Current AIS draught, in meters. */
    val draughtMeters: Double?
        get() = currentPayload.draughtMeters

    /** Current AIS destination. */
    val destination: String?
        get() = currentPayload.destination

    /** Current AIS data terminal ready flag. */
    val dataTerminalReady: Int?
        get() = currentPayload.dataTerminalReady

    /** Current AIS vendor identifier. */
    val vendorId: String?
        get() = currentPayload.vendorId

    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = AISVessel(
        environment,
        node,
        path,
        cycle,
        normalizer,
        *normalizerArgs,
    )

    private class TraceRef(val path: String, val normalizer: String, vararg val args: Any?) {
        private val hash by lazy { Objects.hash(path, normalizer, args.contentHashCode()) }

        override fun hashCode(): Int = hash

        override fun equals(other: Any?) = this === other ||
            (
                other is TraceRef &&
                    path == other.path &&
                    normalizer == other.normalizer &&
                    args.contentDeepEquals(other.args)
                )

        override fun toString(): String = "AISTrace[path=$path, normalizer=$normalizer(${args.contentToString()})]"
    }

    private companion object {
        @Serial
        private const val serialVersionUID = 1L

        private val TRACE_LOADER_CACHE: LoadingCache<TraceRef, AISTraceLoader> = Caffeine
            .newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build { key -> AISTraceLoader(key.path, key.normalizer, *key.args) }

        private val LOADER: LoadingCache<MapEnvironment<*, *, *>, LoadingCache<TraceRef, Iterator<AISTrace>>> = Caffeine
            .newBuilder()
            .weakKeys()
            .build { _ ->
                Caffeine.newBuilder().build { key: TraceRef -> TRACE_LOADER_CACHE.get(key).iterator() }
            }

        fun traceFor(
            environment: MapEnvironment<*, *, *>,
            path: String,
            normalizer: String,
            vararg normalizerArgs: Any?,
        ): AISTrace {
            val aisTraceLoader = checkNotNull(LOADER.get(environment)) {
                "Unable to load an AIS trace mapping for: $environment"
            }
            val key = TraceRef(path, normalizer, *normalizerArgs)
            val iterator = checkNotNull(aisTraceLoader.get(key)) { "Unable to load an AIS trace iterator for: $key" }
            return iterator.takeIf { it.hasNext() }?.next() ?: error("All AIS traces for $key have been consumed.")
        }
    }
}
