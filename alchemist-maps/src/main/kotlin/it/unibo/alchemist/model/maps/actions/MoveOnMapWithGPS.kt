/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.actions

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import it.unibo.alchemist.boundary.gps.loaders.TraceLoader
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.RoutingService
import it.unibo.alchemist.model.RoutingServiceOptions
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.maps.ObjectWithGPS
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy
import java.io.Serial
import java.util.Objects
import java.util.concurrent.TimeUnit

/**
 * Basic action that follows a [GPSTrace].
 */
open class MoveOnMapWithGPS<T, O : RoutingServiceOptions<O>, S : RoutingService<GeoPosition, O>>(
    environment: MapEnvironment<T, O, S>,
    node: Node<T>,
    routingStrategy: RoutingStrategy<T, GeoPosition>,
    speedSelectionStrategy: SpeedSelectionStrategy<T, GeoPosition>,
    targetSelectionStrategy: TargetSelectionStrategy<T, GeoPosition>,
    protected val trace: GPSTrace,
) : MoveOnMap<T, O, S>(environment, node, routingStrategy, speedSelectionStrategy, targetSelectionStrategy) {
    constructor(
        environment: MapEnvironment<T, O, S>,
        node: Node<T>,
        routingStrategy: RoutingStrategy<T, GeoPosition>,
        speedSelectionStrategy: SpeedSelectionStrategy<T, GeoPosition>,
        targetSelectionStrategy: TargetSelectionStrategy<T, GeoPosition>,
        path: String,
        cycle: Boolean,
        normalizer: String,
        vararg normalizerArgs: Any?,
    ) : this(
        environment,
        node,
        routingStrategy,
        speedSelectionStrategy,
        targetSelectionStrategy,
        traceFor(environment, path, cycle, normalizer, *normalizerArgs),
    )

    init {
        sequenceOf(routingStrategy, speedSelectionStrategy, targetSelectionStrategy)
            .filterIsInstance<ObjectWithGPS>()
            .forEach { it.setTrace(trace) }
    }

    private class TraceRef(val path: String, val cycle: Boolean, val normalizer: String, vararg val args: Any?) {
        private val hash by lazy { Objects.hash(path, normalizer, cycle, args.contentHashCode()) }

        override fun hashCode(): Int = hash

        override fun equals(other: Any?) = this === other ||
            (
                other is TraceRef &&
                    path == other.path &&
                    normalizer == other.normalizer &&
                    cycle == other.cycle &&
                    args.contentDeepEquals(other.args)
                )

        override fun toString(): String =
            "${if (cycle) "Cyclic " else ""}Trace[path=$path, normalizer=$normalizer(${args.contentToString()})]"
    }

    /**
     * A cache of trace loaders.
     */
    protected companion object {
        @Serial
        private const val serialVersionUID = 1L
        private val TRACE_LOADER_CACHE: LoadingCache<TraceRef, TraceLoader> =
            Caffeine
                .newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build { key -> TraceLoader(key.path, key.cycle, key.normalizer, *key.args) }

        private val LOADER: LoadingCache<MapEnvironment<*, *, *>, LoadingCache<TraceRef, Iterator<GPSTrace>>> =
            Caffeine
                .newBuilder()
                .weakKeys()
                .build { _ ->
                    Caffeine.newBuilder().build { key: TraceRef -> TRACE_LOADER_CACHE.get(key).iterator() }
                }

        /**
         * Loads a GPS trace from the specified path and normalizer settings.
         */
        @JvmStatic
        fun traceFor(
            environment: MapEnvironment<*, *, *>,
            path: String,
            cycle: Boolean,
            normalizer: String,
            vararg normalizerArgs: Any?,
        ): GPSTrace {
            val gpsTraceLoader =
                checkNotNull(LOADER.get(environment)) {
                    "Unable to load a GPS Trace mapping for: $environment (null was returned)"
                }
            val key = TraceRef(requireNotNull(path), cycle, normalizer, *requireNotNull(normalizerArgs))
            val iter = checkNotNull(gpsTraceLoader.get(key)) { "Unable to load a GPS Trace iterator for: $key" }
            return iter.takeIf { it.hasNext() }?.next() ?: error("All traces for $key have been consumed.")
        }
    }
}
