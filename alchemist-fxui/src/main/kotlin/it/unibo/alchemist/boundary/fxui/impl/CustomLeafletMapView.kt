/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl

import de.saring.leafletmap.LatLong
import de.saring.leafletmap.LeafletMapView
import it.unibo.alchemist.boundary.fxui.util.PointExtension.makePoint
import netscape.javascript.JSObject
import java.awt.Point

/**
 * A [LeafletMapView] that implements functions used to manage the map without any mouse events.
 */
class CustomLeafletMapView : LeafletMapView() {

    companion object {
        /**
         * The maximum zoom level.
         */
        const val MAX_ZOOM_VALUE = 18

        /**
         * The minimum zoom level.
         */
        const val MIN_ZOOM_VALUE = 2

        /**
         * The zoom rate.
         */
        const val ZOOM_RATE = 1

        /**
         * The range of the zoom.
         */
        val ZOOM_RANGE = MIN_ZOOM_VALUE..MAX_ZOOM_VALUE
    }

    init {
        isMouseTransparent = true
    }

    /**
     * Pan the map by a given amount of pixels.
     */
    fun panBy(x: Int, y: Int) {
        execScript("myMap.panBy([$x, $y], { animate: false });")
    }

    /**
     * Set the zoom to the given value without animating.
     */
    fun setZoomWithoutAnimating(zoom: Int) {
        require(zoom in ZOOM_RANGE) {
            "Zoom value was $zoom, not in $ZOOM_RANGE"
        }
        execScript("myMap.setZoom($zoom, { animate: false });")
    }

    /**
     * Retrieve the [LatLong] of the given [Point] on the screen (pixel coordinates).
     */
    fun getLatLongFromPoint(p: Point): LatLong =
        (execScript("myMap.containerPointToLatLng([${p.x}, ${p.y}]);") as JSObject).let {
            LatLong(
                it.getMember("lat").toString().toDouble(),
                it.getMember("lng").toString().toDouble()
            )
        }

    /**
     * Retrieve the [Point] on the screen (in pixel coordinates) of the given [LatLong] position.
     */
    fun getPointFromLatLong(latLong: LatLong): Point =
        (execScript("myMap.latLngToContainerPoint([${latLong.latitude}, ${latLong.longitude}]);") as JSObject).let {
            makePoint(
                it.getMember("x").toString().toDouble(),
                it.getMember("y").toString().toDouble()
            )
        }

    /**
     * Prevent the map from wrapping.
     * Cannot be simply called in the init block
     * since the map does not yet exist at that
     * point in time in the javascript engine.
     */
    fun preventWrapping() {
        execScript("myMap.setMaxBounds([[-90, -180], [90, 180]])")
    }
}
