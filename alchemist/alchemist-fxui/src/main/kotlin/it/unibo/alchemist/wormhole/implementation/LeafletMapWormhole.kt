/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.wormhole.implementation

import de.saring.leafletmap.LatLong
import it.unibo.alchemist.boundary.minus
import it.unibo.alchemist.boundary.monitors.CustomLeafletMapView
import it.unibo.alchemist.boundary.plus
import it.unibo.alchemist.boundary.wormhole.implementation.PointAdapter
import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.model.implementations.positions.LatLongPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.runOnFXThread
import it.unibo.alchemist.syncRunOnFXThread
import javafx.scene.Node
import java.awt.Point

/**
 * The wormhole used for managing a [CustomLeafletMapView].
 */
class LeafletMapWormhole(
    environment: Environment<*, GeoPosition>,
    node: Node,
    private val map: CustomLeafletMapView
) : WormholeFX<GeoPosition>(
    environment,
    node
) {
    init {
        mode = BidimensionalWormhole.Mode.MAP
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
        val env = environment as Environment<Any?, GeoPosition>
        while (
            zoom > CustomLeafletMapView.MIN_ZOOM_VALUE &&
            !env.nodes.parallelStream()
                .map(env::getPosition)
                .map(::getViewPoint)
                .allMatch(::isInsideView)
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
}

/**
 * Converts [this] [GeoPosition] to [LatLong].
 */
fun GeoPosition.toLatLong() = LatLong(latitude, longitude)

/**
 * Converts [this] [LatLong] to [GeoPosition].
 */
fun LatLong.toGeoPosition(): GeoPosition = LatLongPosition(latitude, longitude)
