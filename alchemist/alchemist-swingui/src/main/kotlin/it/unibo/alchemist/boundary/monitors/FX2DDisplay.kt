/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor
import it.unibo.alchemist.boundary.wormhole.implementation.ExponentialZoomManager
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position2D
import java.awt.Point

/**
 * Simple implementation of a monitor that graphically represents a 2D space and simulation.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
</T> */
class FX2DDisplay<T>
@JvmOverloads constructor(step: Int = AbstractFXDisplay.DEFAULT_NUMBER_OF_STEPS) : AbstractFXDisplay<T>(step), FX2DOutputMonitor<T> {
    @Transient
    private var zoomManager: ZoomManager? = null

    /**
     * Lets child-classes access the zoom manager.
     *
     * @return an [ZoomManager]
     */
    protected fun getZoomManager(): ZoomManager? {
        return zoomManager
    }

    /**
     * Lets child-classes change the zoom manager.
     *
     * @param zoomManager an [ZoomManager]
     */
    protected fun setZoomManager(zoomManager: ZoomManager) {
        this.zoomManager = zoomManager
        wormhole.zoom = zoomManager.zoom
    }

    override fun zoomTo(center: Position2D<*>, zoomLevel: Double) {
        assert(center.getDimensions() == 2)
        val wh = wormhole
        wh.zoomOnPoint(wh.getViewPoint(center), zoomLevel)
    }

    override fun initMouseListener() {
        super.initMouseListener()
        interactions!!.input.setOnScroll { event ->
            if (zoomManager != null) {
                zoomManager!!.inc(event.deltaY / ZOOM_SCALE)
                val mouseX = event.x.toInt()
                val mouseY = event.y.toInt()
                wormhole.zoomOnPoint(Point(mouseX, mouseY), zoomManager!!.zoom)
            }
            repaint()
            event.consume()
        }
    }

    override fun init(environment: Environment<T, Position2D<*>>) {
        super.init(environment)
        zoomManager = ExponentialZoomManager(wormhole.zoom, ExponentialZoomManager.DEF_BASE)
    }

    companion object {
        /**
         * Default serial version UID.
         */
        private val serialVersionUID = 1L
        /**
         * Empiric zoom scale value.
         */
        private val ZOOM_SCALE = 40.0
    }
}
