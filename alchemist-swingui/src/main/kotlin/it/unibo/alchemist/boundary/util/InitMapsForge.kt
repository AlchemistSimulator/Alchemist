package it.unibo.alchemist.boundary.util

import org.mapsforge.map.layer.download.tilesource.OpenStreetMapMapnik
import java.io.File

/**
 * Static configurator for Mapsforge, providing a custom user-agent and referer to identify Alchemist
 * on the OpenStreetMaps side.
 */
object InitMapsForge {

    /**
     * Initializes the OpenStreetMap browser agent.
     */
    @JvmStatic
    fun initAgent() {
        with(OpenStreetMapMapnik.INSTANCE) {
            val folderName = (""".*\""" + File.separator + "(.*)").toRegex()
            val experiment = folderName.matchEntire(System.getProperty("user.dir"))
                ?.groups?.get(1)?.value ?: "unkown experiment"
            userAgent = "$experiment via Alchemist Simulator (alchemistsimulator.github.io)"
            referer = "alchemistsimulator.github.io"
        }
    }
}
