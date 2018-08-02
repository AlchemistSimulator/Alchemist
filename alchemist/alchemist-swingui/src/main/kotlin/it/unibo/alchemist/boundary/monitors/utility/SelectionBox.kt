/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors.utility

import it.unibo.alchemist.boundary.wormhole.interfaces.BidimensionalWormhole
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position2D
import javafx.application.Platform
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import java.awt.Point
import kotlin.math.abs
import kotlin.math.min

enum class SelectionBoxState {
    SELECTING,
    SELECTED
}

/**
 * Allows basic multi-element box selections.
 * @param T the concentration of the simulation
 * @param anchorPoint the starting and unchanging [Point] of the selection
 * @param context the [GraphicsContext] used for printing the visual representation of the selection
 * @param wormhole the wormhole of the display being used
 */
class SelectionBox<T>(private val anchorPoint: Point, private val context: GraphicsContext, private val wormhole: BidimensionalWormhole<Position2D<*>>) {
    private val movingPoint = Point(anchorPoint.x, anchorPoint.y)
    var elements: Map<Node<T>, Position2D<*>>? = null
        private set
    private var rectangle = Rectangle()
        get() = anchorPoint.makeRectangleWith(movingPoint)
    var state = SelectionBoxState.SELECTING
        private set

    /**
     * Updates the box, clearing the old one and drawing the updated one.
     * @param newPoint the cursor's new position
     */
    fun update(newPoint: Point): Rectangle = checkFinalized().clear().setNewPoint(newPoint).draw().rectangle

    /**
     * Cancels the current selection
     */
    fun cancel(): SelectionBox<T> = clear()

    /**
     * Locks the elements and writes the items selected to [elements].
     * @param nodes a map having nodes as keys and their positions as values
     */
    fun finalize(nodes: Map<Node<T>, Position2D<*>>): Map<Node<T>, Position2D<*>> =
        checkFinalized().rectangle.let { area ->
            nodes.filterValues { wormhole.getViewPoint(it) in area }
        }.also { elements = it }.also { clear() }.also { state = SelectionBoxState.SELECTED }

    /**
     * Draws the box.
     */
    private fun draw(): SelectionBox<T> = also { rectangle.let {
            Platform.runLater {
                // can edit the elements box's style here
                context.fill = Paint.valueOf("#8e99f3")
                context.fillRect(it.x, it.y, it.width, it.height)
            }
        }
    }

    /**
     * Clears the box.
     */
    private fun clear(): SelectionBox<T> = also { rectangle.let {
        Platform.runLater {
                context.clearRect(it.x, it.y, it.width, it.height)
            }
        }
    }

    /**
     * Sets the moving point to the given point
     */
    private fun setNewPoint(point: Point): SelectionBox<T> = apply {
        movingPoint.x = point.x
        movingPoint.y = point.y
    }

    /**
     * Checks if the selection has been finalized
     * @throws IllegalStateException if the selection has been finalized
     */
    private fun checkFinalized(): SelectionBox<T> {
        if (state == SelectionBoxState.SELECTED) {
            throw IllegalStateException("Selection " + this + " has already been finalized")
        }
        return this
    }
}

private operator fun Rectangle.contains(point: Point): Boolean =
    point.x in this.x..(this.x + this.width) &&
    point.y in this.y..(this.y + this.height)

private fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
    min(this.x, other.x).toDouble(),
    min(this.y, other.y).toDouble(),
    abs(this.x - other.x).toDouble(),
    abs(this.y - other.y).toDouble())
