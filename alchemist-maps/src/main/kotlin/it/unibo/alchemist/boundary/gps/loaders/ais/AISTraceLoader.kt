/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import it.unibo.alchemist.boundary.gps.loaders.AISLoader.Companion.toTraces
import it.unibo.alchemist.boundary.gps.loaders.TraceLoader
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Loads AIS payload streams and applies the same time alignment used by GPS traces.
 */
class AISTraceLoader(
    private val path: String,
    private val normalizer: String,
    private vararg val normalizerArgs: Any?,
) : Iterable<AISTrace> {
    private val traces: List<AISTrace> by lazy { loadTraces() }

    override fun iterator(): Iterator<AISTrace> = traces.iterator()

    private fun loadTraces(): List<AISTrace> {
        val rawTraces = checkNotNull(ResourceLoader.getResourceAsStream(path)) {
            "resource not found: '$path', make sure the file exists in the classpath"
        }.bufferedReader().use { input ->
            AISTrace.from(AISPayload.fromTimedMessages(AISDecoder.parsePayload(input.readText())).values.flatten())
        }
        val alignedTraces = TraceLoader(path, false, normalizer, *normalizerArgs).toList()
        require(rawTraces.size == alignedTraces.size) {
            "AIS metadata traces (${rawTraces.size}) and GPS traces (${alignedTraces.size}) differ for $path"
        }
        return rawTraces.zip(alignedTraces).map { (aisTrace, gpsTrace) ->
            val shift = gpsTrace.startTime.toDouble() -
                aisTrace.firstPositionTimestamp().toEpochMilliseconds() / 1_000.0
            aisTrace.shiftedBy(shift.seconds)
        }
    }

    private fun AISTrace.firstPositionTimestamp(): Instant = positionPayloads.first().timestamp
}
