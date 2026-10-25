/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders

import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import it.unibo.alchemist.boundary.FileFormatException
import it.unibo.alchemist.boundary.gps.GPSFileLoader
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.positions.GPSPointImpl
import it.unibo.alchemist.model.maps.routes.GPSTraceImpl
import it.unibo.alchemist.model.times.DoubleTime
import java.net.URL
import java.util.stream.Collectors
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

/**
 * Class that reads GPS tracks from gpx files.
 */
class GPXLoader : GPSFileLoader {
    override val supportedExtensions: ImmutableSet<String> = EXTENSIONS

    override fun readTrace(url: URL): List<GPSTrace> = url.openStream().use { stream ->
        runCatching {
            GPX.Reader.DEFAULT.read(stream).tracks().map { it.toTrace() }.toList()
        }.getOrElse { throw FileFormatException("GPX", url, it) }
    }

    private fun Track.toTrace(): GPSTrace {
        /*
         * No segments
         */
        check(segments.isNotEmpty()) { "Track $this contains no segment" }
        /*
         * Empty segments
         */
        check(segments().noneMatch { segment -> segment.points.isEmpty() }) {
            "Track $this contains at least a segment with no points"
        }
        /*
         * Converts the Track points to Alchemist GPSPoints
         */
        val points: List<GPSPointImpl> = segments()
            .flatMap { segment -> segment.points() }
            .map { wayPoint ->
                val time = wayPoint.time.map { it.toEpochMilli() / 1000.0 }
                check(time.isPresent) {
                    "Track $this contains at least a waypoint without timestamp: $wayPoint"
                }
                GPSPointImpl(
                    wayPoint.latitude.toDouble(),
                    wayPoint.longitude.toDouble(),
                    DoubleTime(time.get()),
                )
            }.collect(Collectors.toList())
        return GPSTraceImpl(points)
    }

    private companion object {
        private val EXTENSIONS = persistentSetOf("gpx")
    }
}
