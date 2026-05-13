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

/**
 * Reads raw AIS NMEA files as Alchemist GPS traces.
 */
class AISLoader : GPSFileLoader {
    override fun readTrace(url: URL): List<GPSTrace> = url.openStream().use { input ->
        AISPayload
            .from(AISDecoder.parsePayload(input.bufferedReader().readText(), dateFrom(url)))
            .toTraces()
    }

    override fun supportedExtensions(): ImmutableSet<String> = EXTENSIONS

    private fun Iterable<AISPayload>.toTraces(): List<GPSTrace> = groupBy(AISPayload::vesselId)
        .values
        .map { vesselPayloads ->
            GPSTraceImpl(
                vesselPayloads
                    .sortedBy(AISPayload::timestamp)
                    .map {
                        GPSPointImpl(
                            it.latitude,
                            it.longitude,
                            DoubleTime(it.timestamp.toEpochMilli() / MILLIS_IN_SECOND),
                        )
                    },
            )
        }

    private companion object {
        private val EXTENSIONS = ImmutableSet.of("ais", "nmea", "txt")
        private const val MILLIS_IN_SECOND = 1_000.0

        private fun dateFrom(url: URL): String {
            val dateLong = url.path.substringAfterLast("/").substringBefore("-")
            val year = dateLong.take(4)
            val month = dateLong.drop(4).take(2)
            val day = dateLong.takeLast(2)
            return "$year-$month-$day"
        }
    }
}
