/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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

/**
 * Distributes [nodeCount] nodes in the first positions of the [GPSTrace]s found at [path],
 * whose time is aligned using the [normalizer] class created with the given [args].
 * If [nodeCount] is `null`, one node is deployed per available trace.
 * If [cycle] is true and there are more nodes to deploy than available GPS traces,
 * the traces are reused cyclically: e.g., if 10 nodes must be deployed but only 9 GPS traces are available,
 * the first one is reused for the 10th node.
 *
 * @throws IOException if there are errors accessing the file system
 */
class FromGPSTrace @JvmOverloads constructor(
    nodeCount: Int? = null,
    path: String,
    cycle: Boolean = false,
    normalizer: String,
    vararg args: Any,
) : Deployment<GeoPosition> {
    private val traces = TraceLoader(path, cycle, normalizer, *args)

    private val numNode: Int = when (nodeCount) {
        null -> requireNotNull(traces.size) { "The number of nodes must be specified when traces are cyclic" }
        else -> nodeCount.also {
            val traceCount = traces.size
            require(traceCount == null || traceCount >= it) {
                "at least $it traces required, but $traceCount traces available"
            }
        }
    }

    override fun stream(): Stream<GeoPosition> = StreamSupport.stream(traces.spliterator(), false)
        .limit(numNode.toLong())
        .map(GPSTrace::getInitialPosition)
}
