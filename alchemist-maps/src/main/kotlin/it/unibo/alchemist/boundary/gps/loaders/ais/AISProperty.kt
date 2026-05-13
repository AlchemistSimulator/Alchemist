package it.unibo.alchemist.boundary.gps.loaders.ais

import dk.dma.ais.message.AisMessage
import dk.dma.ais.message.AisMessage5
import dk.dma.ais.message.AisPositionMessage
import dk.dma.ais.message.AisStaticCommon
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload.Companion.vesselPosition

enum class AISProperty {
    /**
     * Maritime Mobile Service Identity
     */
    MMSI,

    /**
     * Data timestamp AIS format
     */
    TIME,

    /**
     * Geographical longitude
     */
    LONGITUDE,

    /**
     * Geographical latitude
     */
    LATITUDE,

    /**
     * Speed over ground, expressed in knots.
     */
    SOG,

    /**
     * Course over ground, expressed in degrees.
     */
    COG,

    /**
     * Vessel heading, expressed in degrees.
     */
    HEADING,

    /**
     * Raw AIS position accuracy flag.
     */
    PAC,

    /**
     * Rate of turn, expressed according to the AIS library conversion.
     */
    ROT,

    /**
     * Raw AIS navigational status.
     */
    NAVSTAT,

    /**
     * IMO ship identification number
     */
    IMO,

    /**
     * Vessel's name
     */
    NAME,

    /**
     * Vessel's callsign
     */
    CALLSIGN,

    /**
     * Vessel's type
     */
    TYPE,

    /**
     * Positioning device type
     */
    DEVICE,

    /**
     * Raw AIS RAIM flag.
     */
    RAIM,
    ;

    internal fun extract(message: AisMessage): Double? = when (this) {
        MMSI -> message.userId.toDouble()
        TYPE -> (message as? AisStaticCommon)?.shipType?.toDouble()
        SOG -> message.vesselPosition()?.takeIf { it.isSogValid }?.sog?.div(DIV)
        COG -> message.vesselPosition()?.takeIf { it.isCogValid }?.cog?.div(DIV)
        HEADING -> message.vesselPosition()?.takeIf { it.isHeadingValid }?.trueHeading?.toDouble()
        RAIM -> message.vesselPosition()?.raim?.toDouble()
        PAC -> message.vesselPosition()?.posAcc?.toDouble()
        ROT -> (message as? AisPositionMessage)?.takeIf { it.isRotValid }?.rot?.toDouble()
        NAVSTAT -> (message as? AisPositionMessage)?.navStatus?.toDouble()
        IMO -> (message as? AisMessage5)?.imo?.toDouble()
        else -> null
    }

    companion object {
        val DEFAULT: Set<AISProperty> = setOf(SOG, COG, HEADING, PAC, ROT, NAVSTAT, RAIM, TYPE)
        private const val DIV = 10.0
    }
}
