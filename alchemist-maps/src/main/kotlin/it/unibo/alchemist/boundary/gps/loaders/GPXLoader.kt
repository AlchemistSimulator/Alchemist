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
import io.jenetics.jpx.GPX
import io.jenetics.jpx.Track
import it.unibo.alchemist.boundary.gps.GPSFileLoader
import it.unibo.alchemist.model.maps.GPSTrace
import it.unibo.alchemist.model.maps.positions.GPSPointImpl
import it.unibo.alchemist.model.maps.routes.GPSTraceImpl
import it.unibo.alchemist.model.times.DoubleTime
import org.openstreetmap.osmosis.osmbinary.file.FileFormatException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.Objects
import java.util.stream.Collectors

/**
 * Class that reads GPS tracks from gpx files.
 */
class GPXLoader : GPSFileLoader {

    override fun readTrace(url: URL): List<GPSTrace> = url.openStream().use { stream ->
        getGPX(stream)
            .tracks()
            .map { track: Track -> getTrace(Objects.requireNonNull(track, "GPS track not found")) }
            .collect(Collectors.toList())
    }

    override fun supportedExtensions(): ImmutableSet<String> {
        return EXTENSIONS
    }

    private fun getGPX(stream: InputStream): GPX {
        return try {
            GPX.Reader.DEFAULT.read(stream)
        } catch (e: IOException) {
            val realException = FileFormatException("Cannot read the GPX content. Please make sure it is a valid GPX.")
            realException.initCause(e)
            throw realException
        }
    }

    private fun getTrace(track: Track): GPSTrace {
        /*
         * No segments
         */
        check(track.segments.isNotEmpty()) { "Track $track contains no segment" }
        /*
         * Empty segments
         */
        check(track.segments().noneMatch { segment -> segment.points.isEmpty() }) {
            "Track $track contains at least a segment with no points"
        }
        /*
         * Converts the Track points to Alchemist GPSPoints
         */
        val points: List<GPSPointImpl> = track.segments()
            .flatMap { segment -> segment.points() }
            .map { wayPoint ->
                val time = wayPoint.time.map { it.toEpochMilli() / 1000.0 }
                check(time.isPresent) {
                    "Track $track contains at least a waypoint without timestamp: $wayPoint"
                }
                GPSPointImpl(
                    wayPoint.latitude.toDouble(),
                    wayPoint.longitude.toDouble(),
                    DoubleTime(time.get()),
                )
            }
            .collect(Collectors.toList())
        return GPSTraceImpl(points)
    }

    companion object {
        private val EXTENSIONS = ImmutableSet.of("gpx")
    }
}
