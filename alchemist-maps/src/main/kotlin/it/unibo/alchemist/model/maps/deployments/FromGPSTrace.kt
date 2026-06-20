/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.deployments

import it.unibo.alchemist.boundary.gps.loaders.TraceLoader
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.maps.GPSTrace
import java.io.IOException
import java.util.stream.Stream
import java.util.stream.StreamSupport
import kotlin.jvm.optionals.getOrDefault

/**
 * Distributes nodes in the first positions of [GPSTrace].
 */
class FromGPSTrace private constructor(private val traces: TraceLoader, private val numNode: Int) :
    Deployment<GeoPosition> {

    /**
     * @param nodeCount number of node requests
     * @param path path with the gps tracks
     * @param cycle true if, in case there are more nodes to deploy than available GPS traces,
     * the traces should be reused cyclically. E.g., if 10 nodes must be deployed but only 9 GPS
     * traces are available, the first one is reused for the 10th node.
     * @param normalizer class to use to normalize time
     * @param args args to use to create GPSTimeNormalizer
     * @throws IOException if there are errors accessing the file system
     */
    @Throws(IOException::class)
    constructor(
        nodeCount: Int,
        path: String,
        cycle: Boolean,
        normalizer: String,
        vararg args: Any,
    ) : this(TraceLoader(path, cycle, normalizer, *args), nodeCount) {
        requireEnoughTraces(traces, nodeCount)
    }

    /**
     * Creates a deployment with one node per loaded GPS trace.
     *
     * @param path path with the gps tracks
     * @param normalizer class to use to normalize time
     * @param args args to use to create GPSTimeNormalizer
     * @throws IOException if there are errors accessing the file system
     */
    @Throws(IOException::class)
    constructor(
        path: String,
        normalizer: String,
        vararg args: Any,
    ) : this(loadWithSize(path, normalizer, *args))

    private constructor(tracesWithSize: Pair<TraceLoader, Int>) : this(tracesWithSize.first, tracesWithSize.second)

    override fun stream(): Stream<GeoPosition> = StreamSupport.stream(traces.spliterator(), false)
        .limit(numNode.toLong())
        .map(GPSTrace::getInitialPosition)

    private companion object {
        @Throws(IOException::class)
        private fun loadWithSize(path: String, normalizer: String, vararg args: Any): Pair<TraceLoader, Int> {
            val traces = TraceLoader(path, normalizer, *args)
            return traces to traces.size().orElseThrow()
        }

        private fun requireEnoughTraces(traces: TraceLoader, nodeCount: Int) {
            val traceCount = traces.size()
            require(traceCount.getOrDefault(Int.MAX_VALUE) >= nodeCount) {
                "$nodeCount traces required, ${traceCount.orElse(-1)} traces available"
            }
        }
    }
}
