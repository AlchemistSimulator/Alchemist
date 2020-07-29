/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.interactions

import it.unibo.alchemist.boundary.intersectingNodes
import it.unibo.alchemist.boundary.makePoint
import it.unibo.alchemist.boundary.makeRectangleWith
import it.unibo.alchemist.boundary.wormhole.interfaces.Wormhole2D
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import java.awt.Point

/**
 * Manages multi-element selection and click-selection.
 */
class SelectionHelper<T, P : Position2D<P>> {

    /**
     * Allows basic multi-element box selections.
     * @param anchorPoint the starting and unchanging [Point] of the selection
     */
    class SelectionBox(val anchorPoint: Point, private val movingPoint: Point = anchorPoint) {
        /**
         * The rectangle representing the box.
         * If the rectangle's dimensions are (0, 0), the rectangle is to be considered non-existing.
         */
        val rectangle
            get() = anchorPoint.makeRectangleWith(movingPoint)

        override fun toString(): String = "[$anchorPoint, $movingPoint]"
    }

    private var box: SelectionBox? = null
    private var selectionPoint: Point? = null
    private var isSelecting = false

    /**
     * The rectangle representing the box.
     * If the rectangle's dimensions are (0, 0), the rectangle is to be considered non-existing.
     */
    val rectangle
        get() = box?.rectangle ?: makePoint(0, 0).let { it.makeRectangleWith(it) }

    /**
     * Begins a new selection at the given point.
     */
    fun begin(point: Point): SelectionHelper<T, P> = apply {
        isSelecting = true
        selectionPoint = point
        box = SelectionBox(point)
    }

    /**
     * Updates the selection with a new point.
     */
    fun update(point: Point): SelectionHelper<T, P> = apply {
        if (isSelecting) {
            box?.let {
                box = SelectionBox(
                    it.anchorPoint,
                    point
                )
            }
            selectionPoint = null
        }
    }

    /**
     * Closes the selection.
     */
    fun close() {
        box = null
        selectionPoint = null
        isSelecting = false
    }

    /**
     * Retrieves the element selected by clicking. If selection was not done by clicking, null.
     */
    fun clickSelection(
        nodes: Map<Node<T>, P>,
        wormhole: Wormhole2D<P>
    ): Pair<Node<T>, P>? =
        selectionPoint?.let { point ->
            nodes.minBy { nodes[it.key]!!.distanceTo(wormhole.getEnvPoint(point)) }?.let {
                Pair(it.key, it.value)
            }
        }

    /**
     * Retrieves the elements selected by box selection, thus possibly empty.
     */
    fun boxSelection(
        nodes: Map<Node<T>, P>,
        wormhole: Wormhole2D<P>
    ): Map<Node<T>, P> =
        box?.let {
            rectangle.intersectingNodes(nodes, wormhole)
        } ?: emptyMap()
}
