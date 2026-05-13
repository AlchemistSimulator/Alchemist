package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.IPositionMessage
import dk.dma.ais.message.IVesselPositionMessage
import java.time.Instant

/**
 * Subset of AIS information used to generate traces.
 *
 * @property vesselId AIS MMSI.
 * @property timestamp the timestamp related to the receipt of the message.
 * @property longitude longitude of the boat.
 * @property latitude latitude of the boat.
 * @property properties numeric AIS properties.
 */
data class AISPayload(
    val vesselId: Int,
    val timestamp: Instant,
    val longitude: Double,
    val latitude: Double,
    val properties: Map<AISProperty, Double>,
) {
    /**
     * Static factory for [AISPayload]
     */
    companion object {
        /**
         * Builds an [AISPayload] from an AIS message when it carries a valid position.
         */
        fun from(
            timestamp: Instant,
            message: AisMessage,
            properties: Set<AISProperty> = AISProperty.DEFAULT,
        ): AISPayload? {
            val positionMessage = message as? IPositionMessage ?: return null
            val longitude = positionMessage.pos.longitudeDouble
            val latitude = positionMessage.pos.latitudeDouble
            return if (longitude.isValidLongitude() && latitude.isValidLatitude()) {
                AISPayload(
                    vesselId = message.userId,
                    timestamp = timestamp,
                    longitude = longitude,
                    latitude = latitude,
                    properties = properties.mapNotNull { property ->
                        property.extract(message)?.let { property to it }
                    }.toMap(),
                )
            } else {
                null
            }
        }

        /**
         * Converts timestamped AIS messages to payloads.
         */
        fun from(
            messages: Map<Instant, AisMessage>,
            properties: Set<AISProperty> = AISProperty.DEFAULT,
        ): List<AISPayload> = messages
            .mapNotNull { (timestamp, message) -> from(timestamp, message, properties) }
            .sortedWith(compareBy(AISPayload::vesselId, AISPayload::timestamp))

        internal fun AisMessage.vesselPosition(): IVesselPositionMessage? = this as? IVesselPositionMessage

        private fun Double.isValidLatitude(): Boolean = !isNaN() && this in -90.0..90.0

        private fun Double.isValidLongitude(): Boolean = !isNaN() && this in -180.0..180.0
    }
}
