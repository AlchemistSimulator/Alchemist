/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.interactions

import it.unibo.alchemist.boundary.jfx.events.keyboard.KeyboardActionListener
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D
import it.unibo.alchemist.boundary.wormhole.interfaces.ZoomManager
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.scene.Group

/**
 * An interaction manager that controls the input/output on the environment done through the GUI.
 */
interface InteractionManager<T, P : Position2D<P>> {
    /**
     * The current environment.
     */
    var environment: Environment<T, P>

    /**
     * The nodes in the environment.
     * Should be updated as frequently as possible to ensure a representation of
     * the feedback that is consistent with the actual environment.
     */
    var nodes: Map<Node<T>, P>

    /**
     * The keyboard listener.
     */
    val keyboardListener: KeyboardActionListener

    /**
     * The canvases used for input/output.
     */
    val canvases: Group

    /**
     * Clears and then paints every currently active feedback.
     */
    fun repaint(): Unit

    /**
     * Sets the wormhole.
     */
    fun setWormhole(wormhole: Wormhole2D<P>): Unit

    /**
     * Sets the zoom manager.
     */
    fun setZoomManager(zoomManager: ZoomManager): Unit

    /**
     * To be called whenever the monitor paints its effects.
     */
    fun onMonitorRepaint(): Unit
}
