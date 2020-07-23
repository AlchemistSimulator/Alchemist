/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import de.saring.leafletmap.LatLong
import de.saring.leafletmap.LeafletMapView
import de.saring.leafletmap.MapConfig
import de.saring.leafletmap.ZoomControlConfig
import it.unibo.alchemist.boundary.monitors.CustomLeafletMapView.Companion.MAX_ZOOM_VALUE
import it.unibo.alchemist.boundary.monitors.CustomLeafletMapView.Companion.MIN_ZOOM_VALUE
import it.unibo.alchemist.boundary.monitors.CustomLeafletMapView.Companion.ZOOM_RATE
import it.unibo.alchemist.boundary.wormhole.implementation.LinearZoomManager
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.boundary.makePoint
import it.unibo.alchemist.runOnFXThread
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.wormhole.implementation.LeafletMapWormhole
import javafx.concurrent.Worker
import netscape.javascript.JSObject
import java.awt.Point
import java.util.concurrent.CompletableFuture

/**
 * Simple implementation of a monitor that graphically represents a simulation on a 2D map, specifically LeafletMap.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
 */
class LeafletMapDisplay<T>
@JvmOverloads constructor(step: Int = DEFAULT_NUMBER_OF_STEPS) : AbstractFXDisplay<T, GeoPosition>(step) {
    private val map = CustomLeafletMapView()
    private val mapLoading: CompletableFuture<Worker.State>

    init {
        map.prefWidthProperty().bind(widthProperty())
        map.prefHeightProperty().bind(heightProperty())
        background.children.add(map)
        mapLoading = map.displayMap(MapConfig(zoomControlConfig = ZoomControlConfig(false)))
    }

    override fun init(environment: Environment<T, GeoPosition>) {
        if (!mapLoading.isDone) {
            mapLoading.join()
        }
        runOnFXThread {
            map.preventWrapping()
        }
        super.init(environment)
    }

    override fun createWormhole(environment: Environment<T, GeoPosition>) =
        LeafletMapWormhole(environment, this, map)

    override fun createZoomManager(wormhole: BidimensionalWormhole<GeoPosition>) =
        LinearZoomManager(
            MAX_ZOOM_VALUE.toDouble(),
            ZOOM_RATE.toDouble(),
            MIN_ZOOM_VALUE.toDouble(),
            MAX_ZOOM_VALUE.toDouble()
        )
}

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
        if (zoom !in ZOOM_RANGE) {
            throw IllegalArgumentException("Zoom value was $zoom, not between $ZOOM_RANGE")
        } else {
            execScript("myMap.setZoom($zoom, { animate: false });")
        }
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
     * Cannot be simply called in the init block since the map
     * does not yet exist at that point the javascript engine.
     */
    fun preventWrapping() {
        execScript("myMap.setMaxBounds([[-90, -180], [90, 180]])")
    }
}
