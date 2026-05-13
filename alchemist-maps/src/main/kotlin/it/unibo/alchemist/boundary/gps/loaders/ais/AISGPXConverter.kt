package it.unibo.alchemist.boundary.gps.loaders.ais

import io.jenetics.jpx.GPX
import io.jenetics.jpx.Speed
import io.jenetics.jpx.Track
import io.jenetics.jpx.TrackSegment
import io.jenetics.jpx.WayPoint
import java.nio.file.Files
import java.nio.file.Path

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
        payloads
            .groupBy(AISPayload::vesselId)
            .forEach { (vesselId, points) ->
                val anonymizedId = vesselIdMapper(vesselId)
                val track = Track
                    .builder()
                    .name("Vessel $anonymizedId")
                    .addSegment(TrackSegment.of(points.sortedBy(AISPayload::timestamp).map { it.toWayPoint() }))
                    .build()
                GPX.write(
                    GPX.builder("Alchemist AIS importer").addTrack(track).build(),
                    outputDirectory.resolve("$anonymizedId.gpx"),
                )
            }
    }

    private fun AISPayload.toWayPoint(): WayPoint {
        val builder = WayPoint
            .builder()
            .lat(latitude)
            .lon(longitude)
            .time(timestamp)
        properties[AISProperty.SOG]?.let { builder.speed(it, Speed.Unit.KNOTS) }
        properties[AISProperty.COG]?.let { builder.course(it) }
        return builder.build()
    }
}
