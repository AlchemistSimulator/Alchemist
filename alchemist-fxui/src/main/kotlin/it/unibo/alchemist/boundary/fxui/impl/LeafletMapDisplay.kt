/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.impl

import de.saring.leafletmap.MapConfig
import de.saring.leafletmap.ZoomControlConfig
import it.unibo.alchemist.boundary.fxui.util.JavaFXThreadUtil
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.boundary.ui.impl.LinearZoomManager
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GeoPosition
import javafx.concurrent.Worker
import java.util.concurrent.CompletableFuture

/**
 * Simple implementation of a monitor that graphically represents a simulation on a 2D map, specifically LeafletMap.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
 */
class LeafletMapDisplay<T> : AbstractFXDisplay<T, GeoPosition>() {
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
        JavaFXThreadUtil.runOnFXThread {
            map.preventWrapping()
        }
        super.init(environment)
    }

    override fun createWormhole(environment: Environment<T, GeoPosition>) =
        LeafletMapWormhole(environment, this, map)

    override fun createZoomManager(wormhole: Wormhole2D<GeoPosition>) =
        LinearZoomManager(
            CustomLeafletMapView.MAX_ZOOM_VALUE.toDouble(),
            CustomLeafletMapView.ZOOM_RATE.toDouble(),
            CustomLeafletMapView.MIN_ZOOM_VALUE.toDouble(),
            CustomLeafletMapView.MAX_ZOOM_VALUE.toDouble()
        )
}
