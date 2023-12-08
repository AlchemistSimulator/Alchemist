/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.util

import it.unibo.alchemist.boundary.fxui.util.PointExtension.makeRectangleWith
import it.unibo.alchemist.boundary.fxui.util.RectangleExtension.intersectingNodes
import it.unibo.alchemist.boundary.ui.api.Wormhole2D
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import javafx.scene.shape.Rectangle
import java.awt.Point

/**
 * Manages multi-element selection and click-selection.
 */
class SelectionHelper<T, P : Position2D<P>> {

    /**
     * Allows basic multi-element box selections.
     * @param anchorPoint the starting and unchanging [Point] of the selection
     */
    class SelectionBox(val anchorPoint: Point = Point(0, 0), private val movingPoint: Point = anchorPoint) {
        /**
         * The rectangle representing the box.
         * If the rectangle's dimensions are (0, 0), the rectangle is to be considered non-existing.
         */
        val rectangle
            get() = when {
                closed -> Rectangle(0.0, 0.0, 0.0, 0.0)
                else -> anchorPoint.makeRectangleWith(movingPoint)
            }

        /**
         * Returns whether the SelectionBox has been closed.
         */
        var closed = false
            private set

        /**
         * Closes this SelectionBox.
         */
        fun close() {
            closed = true
        }

        override fun toString(): String = "[$anchorPoint, $movingPoint]"
    }

    private var box = SelectionBox().apply { close() }
    private var selectionPoint: Point? = null

    /**
     * Returns whether a box selection is currently occurring.
     */
    var isBoxSelectionInProgress = false
        private set

    /**
     * The rectangle representing the box.
     * If the rectangle's dimensions are (0, 0), the rectangle is to be considered non-existing.
     */
    val rectangle
        get() = when {
            box.closed -> Point(0, 0).let { it.makeRectangleWith(it) }
            else -> box.rectangle
        }

    /**
     * Begins a new selection at the given point.
     */
    fun begin(point: Point): SelectionHelper<T, P> = apply {
        isBoxSelectionInProgress = true
        selectionPoint = point
        box = SelectionBox(point)
    }

    /**
     * Updates the selection with a new point.
     */
    fun update(point: Point): SelectionHelper<T, P> = apply {
        if (isBoxSelectionInProgress && !box.closed) {
            box = SelectionBox(box.anchorPoint, point)
            selectionPoint = null
        }
    }

    /**
     * Closes the selection.
     */
    fun close() {
        box.close()
        selectionPoint = null
        isBoxSelectionInProgress = false
    }

    /**
     * Retrieves the element selected by clicking. If selection was not done by clicking, null.
     */
    fun clickSelection(
        nodes: Map<Node<T>, P>,
        wormhole: Wormhole2D<P>,
    ): Pair<Node<T>, P>? =
        selectionPoint?.let { point ->
            nodes.minByOrNull { nodes[it.key]?.distanceTo(wormhole.getEnvPoint(point)) ?: Double.POSITIVE_INFINITY }
                ?.toPair()
        }

    /**
     * Retrieves the elements selected by box selection, thus possibly empty.
     */
    fun boxSelection(
        nodes: Map<Node<T>, P>,
        wormhole: Wormhole2D<P>,
    ): Map<Node<T>, P> =
        when {
            box.closed -> emptyMap()
            else -> rectangle.intersectingNodes(nodes, wormhole)
        }
}
