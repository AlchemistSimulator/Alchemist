/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.maps

import de.saring.leafletmap.LatLong
import it.unibo.alchemist.boundary.fxui.WormholeFX
import it.unibo.alchemist.boundary.fxui.util.JavaFXThreadUtil.runOnFXThread
import it.unibo.alchemist.boundary.fxui.util.JavaFXThreadUtil.syncRunOnFXThread
import it.unibo.alchemist.boundary.fxui.util.PointExtension.minus
import it.unibo.alchemist.boundary.fxui.util.PointExtension.plus
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.boundary.ui.impl.PointAdapter
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.implementations.positions.LatLongPosition
import javafx.scene.Node
import java.awt.Point

/**
 * The wormhole used for managing a [CustomLeafletMapView].
 */
class LeafletMapWormhole(
    environment: Environment<*, GeoPosition>,
    node: Node,
    private val map: CustomLeafletMapView,
) : WormholeFX<GeoPosition>(
    environment,
    node,
) {
    init {
        mode = Wormhole2D.Mode.MAP
    }

    override fun getEnvPoint(viewPoint: Point): GeoPosition =
        syncRunOnFXThread {
            map.getLatLongFromPoint(viewPoint).toGeoPosition()
        }

    override fun getViewPoint(envPoint: GeoPosition): Point =
        syncRunOnFXThread {
            map.getPointFromLatLong(envPoint.toLatLong())
        }

    override fun rotateAroundPoint(p: Point?, a: Double) =
        throw UnsupportedOperationException()

    override fun setEnvPosition(envPoint: GeoPosition) {
        position = PointAdapter.from(getViewPoint(envPoint))
        runOnFXThread {
            map.panTo(envPoint.toLatLong())
        }
    }

    override fun setViewPosition(viewPoint: Point) {
        val movement = viewPosition - viewPoint
        position = position.sum(PointAdapter.from<GeoPosition>(movement))
        runOnFXThread {
            map.panBy(movement.x, movement.y)
        }
    }

    override fun optimalZoom() {
        zoom = CustomLeafletMapView.MAX_ZOOM_VALUE.toDouble()
        @Suppress("UNCHECKED_CAST")
        val environment = environment as Environment<Any?, GeoPosition>
        while (
            zoom > CustomLeafletMapView.MIN_ZOOM_VALUE &&
            !environment.nodes
                .map(environment::getPosition)
                .map(::getViewPoint)
                .all(::isInsideView)
        ) {
            zoom--
        }
    }

    override fun setZoom(zoom: Double) {
        zoom.toInt().takeIf(CustomLeafletMapView.ZOOM_RANGE::contains)?.let { intZoom ->
            super.setZoom(intZoom.toDouble())
            runOnFXThread {
                map.setZoomWithoutAnimating(intZoom)
            }
        }
    }

    override fun zoomOnPoint(point: Point, zoomRate: Double) {
        val envPoint = getEnvPoint(point)
        zoom = zoomRate
        val newViewCenter = getViewPoint(envPoint)
        val delta = point - newViewCenter
        viewPosition += delta
    }

    companion object {
        /**
         * Converts [this] [GeoPosition] to [LatLong].
         */
        private fun GeoPosition.toLatLong() = LatLong(latitude, longitude)

        /**
         * Converts [this] [LatLong] to [GeoPosition].
         */
        private fun LatLong.toGeoPosition(): GeoPosition = LatLongPosition(latitude, longitude)
    }
}
