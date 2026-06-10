/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import io.jenetics.jpx.GPX
import io.jenetics.jpx.Speed
import io.jenetics.jpx.Track
import io.jenetics.jpx.TrackSegment
import io.jenetics.jpx.WayPoint
import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.toJavaInstant

/**
 * Converts decoded AIS payloads into one GPX file per vessel.
 */
class AISGPXConverter {
    /**
     * Writes GPX traces grouped by vessel.
     *
     * @param payloads AIS payloads to export.
     * @param outputDirectory destination directory.
     * @param vesselIdMapper mapping used to avoid writing raw MMSIs when anonymization is desired.
     */
    fun write(payloads: Iterable<AISPayload>, outputDirectory: Path, vesselIdMapper: (Int) -> String = Int::toString) {
        Files.createDirectories(outputDirectory)
        AISVesselStatus.from(payloads)
            .forEach { (vesselId, points) ->
                val anonymizedId = vesselIdMapper(vesselId)
                val track = Track
                    .builder()
                    .name("Vessel $anonymizedId")
                    .addSegment(TrackSegment.of(points.sortedBy(AISVesselStatus::timestamp).map { it.toWayPoint() }))
                    .build()
                GPX.write(
                    GPX.builder("Alchemist AIS importer").addTrack(track).build(),
                    outputDirectory.resolve("$anonymizedId.gpx"),
                )
            }
    }

    private fun AISVesselStatus.toWayPoint(): WayPoint? = when {
        latitude == null -> null
        longitude == null -> null
        else -> {
            val wayPointBuilder = WayPoint.builder()
                .lat(latitude)
                .lon(longitude)
                .time(timestamp.toJavaInstant())
            speedOverGroundKnots?.let { wayPointBuilder.speed(it, Speed.Unit.KNOTS) }
            courseOverGroundDegrees?.let { wayPointBuilder.course(it) }
            wayPointBuilder.build()
        }
    }
}
