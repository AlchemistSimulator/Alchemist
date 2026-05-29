/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
import it.unibo.alchemist.boundary.gps.loaders.ais.AISVesselStatus
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.positions.GPSPointImpl
import it.unibo.alchemist.model.maps.routes.GPSTraceImpl
import it.unibo.alchemist.model.times.DoubleTime
import java.net.URL
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.Instant
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException

/**
 * Reads raw AIS NMEA files as Alchemist GPS traces.
 */
class AISLoader : GPSFileLoader {
    override fun readTrace(url: URL): List<GPSTrace> = runCatching {
        url.openStream().use { input ->
            val content = input.bufferedReader().readText()
            AISVesselStatus.from(AISPayload.from(AISDecoder.parsePayload(content))).toTraces()
        }
    }.getOrElse {
        throw FileFormatException("Incorrect AIS Payload in $url").initCause(it)
    }

    override fun supportedExtensions(): ImmutableSet<String> = EXTENSIONS

    /**
     * AIS trace conversion helpers.
     */
    companion object {
        private val EXTENSIONS = ImmutableSet.of("ais", "nmea", "txt")
        private val EPOCH = Instant.fromEpochSeconds(0)

        /**
         * Converts AIS payloads to GPS traces, preserving epoch-based times by default.
         *
         * @param timeOrigin instant mapped to simulation time zero.
         */
        internal fun Iterable<AISPayload>.toTraces(timeOrigin: Instant = EPOCH): List<GPSTrace> =
            AISVesselStatus.from(this).toTraces(timeOrigin)

        internal fun Map<AISPayload.MMSI, List<AISVesselStatus>>.toTraces(timeOrigin: Instant = EPOCH): List<GPSTrace> =
            values.map { vesselPayloads ->
                GPSTraceImpl(
                    vesselPayloads.sortedBy(AISVesselStatus::timestamp).map {
                        GPSPointImpl(
                            it.latitude,
                            it.longitude,
                            it.timestamp.toTraceTime(timeOrigin),
                        )
                    },
                )
            }

        private fun Instant.toTraceTime(timeOrigin: Instant): DoubleTime = DoubleTime(
            (this - timeOrigin).toDouble(SECONDS),
        )
    }
}
