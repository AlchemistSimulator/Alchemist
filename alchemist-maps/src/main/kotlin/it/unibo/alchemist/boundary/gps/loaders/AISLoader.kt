/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders

import com.google.common.collect.ImmutableSet
import it.unibo.alchemist.boundary.gps.GPSFileLoader
import it.unibo.alchemist.boundary.gps.loaders.ais.AISDecoder
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.positions.GPSPointImpl
import it.unibo.alchemist.model.maps.routes.GPSTraceImpl
import it.unibo.alchemist.model.times.DoubleTime
import java.net.URL
import kotlin.time.Instant

/**
 * Reads raw AIS NMEA files as Alchemist GPS traces.
 */
class AISLoader : GPSFileLoader {
    override fun readTrace(url: URL): List<GPSTrace> = url.openStream().use { input ->
        val date = AISDecoder.dateFrom(url.path.substringAfterLast("/"))
        AISPayload
            .from(AISDecoder.parsePayload(input.bufferedReader().readText(), date))
            .toTraces()
    }

    override fun supportedExtensions(): ImmutableSet<String> = EXTENSIONS
}

/**
 * Converts AIS payloads to GPS traces, preserving epoch-based times by default.
 *
 * @param timeOrigin instant mapped to simulation time zero.
 */
internal fun Iterable<AISPayload>.toTraces(timeOrigin: Instant = EPOCH): List<GPSTrace> = groupBy(
    AISPayload::vesselId,
)
    .values
    .map { vesselPayloads ->
        GPSTraceImpl(
            vesselPayloads
                .sortedBy(AISPayload::timestamp)
                .map {
                    GPSPointImpl(
                        it.latitude,
                        it.longitude,
                        it.timestamp.toTraceTime(timeOrigin),
                    )
                },
        )
    }

private fun Instant.toTraceTime(timeOrigin: Instant): DoubleTime = DoubleTime(
    (this - timeOrigin).inWholeMilliseconds / MILLIS_IN_SECOND,
)

private val EXTENSIONS = ImmutableSet.of("ais", "nmea", "txt")
private val EPOCH = Instant.fromEpochSeconds(0)
private const val MILLIS_IN_SECOND = 1_000.0
