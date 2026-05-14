package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisPositionMessage
import dk.dma.ais.message.AisStaticCommon
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
 * @property sog speed over ground, expressed in knots.
 * @property cog course over ground, expressed in degrees.
 * @property heading vessel heading, expressed in degrees.
 * @property positionAccuracy raw AIS position accuracy flag.
 * @property rateOfTurn rate of turn, expressed according to the AIS library conversion.
 * @property navigationalStatus raw AIS navigational status.
 * @property raim raw AIS RAIM flag.
 * @property shipType vessel type.
 */
data class AISPayload(
    val vesselId: Int,
    val timestamp: Instant,
    val longitude: Double,
    val latitude: Double,
    val sog: Double? = null,
    val cog: Double? = null,
    val heading: Double? = null,
    val positionAccuracy: Double? = null,
    val rateOfTurn: Double? = null,
    val navigationalStatus: Double? = null,
    val raim: Double? = null,
    val shipType: Double? = null,
) {
    /**
     * Static factory for [AISPayload]
     */
    companion object {
        /**
         * Builds an [AISPayload] from an AIS message when it carries a valid position.
         */
        fun from(timestamp: Instant, message: AisMessage): AISPayload? {
            val positionMessage = message as? IPositionMessage ?: return null
            val longitude = positionMessage.pos.longitudeDouble
            val latitude = positionMessage.pos.latitudeDouble
            val vesselPosition = message.vesselPosition()
            return if (longitude.isValidLongitude() && latitude.isValidLatitude()) {
                AISPayload(
                    vesselId = message.userId,
                    timestamp = timestamp,
                    longitude = longitude,
                    latitude = latitude,
                    sog = vesselPosition?.takeIf { it.isSogValid }?.sog?.div(DIV),
                    cog = vesselPosition?.takeIf { it.isCogValid }?.cog?.div(DIV),
                    heading = vesselPosition?.takeIf { it.isHeadingValid }?.trueHeading?.toDouble(),
                    positionAccuracy = vesselPosition?.posAcc?.toDouble(),
                    rateOfTurn = (message as? AisPositionMessage)?.takeIf { it.isRotValid }?.rot?.toDouble(),
                    navigationalStatus = (message as? AisPositionMessage)?.navStatus?.toDouble(),
                    raim = vesselPosition?.raim?.toDouble(),
                    shipType = (message as? AisStaticCommon)?.shipType?.toDouble(),
                )
            } else {
                null
            }
        }

        /**
         * Converts timestamped AIS messages to payloads.
         */
        fun from(messages: Map<Instant, AisMessage>): List<AISPayload> = messages
            .mapNotNull { (timestamp, message) -> from(timestamp, message) }
            .sortedWith(compareBy(AISPayload::vesselId, AISPayload::timestamp))

        private fun AisMessage.vesselPosition(): IVesselPositionMessage? = this as? IVesselPositionMessage

        private fun Double.isValidLatitude(): Boolean = !isNaN() && this in -90.0..90.0

        private fun Double.isValidLongitude(): Boolean = !isNaN() && this in -180.0..180.0

        private const val DIV = 10.0
    }
}
