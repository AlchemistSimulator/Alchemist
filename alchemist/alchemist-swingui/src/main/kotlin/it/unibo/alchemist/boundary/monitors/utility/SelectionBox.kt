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

/**
 * Allows basic multi-element box selections.
 * @param T the concentration of the simulation
 */
class SelectionBox<T>
/**
 * @param anchorPoint the starting and unchanging [Point] of the selection
 * @param context the [GraphicsContext] used for printing the visual representation of the selection
 * @param wormhole the wormhole of the display being used
 */
(private val anchorPoint: Point, private val context: GraphicsContext, private val wormhole: BidimensionalWormhole<Position2D<*>>) {
    private val movingPoint = Point(anchorPoint.x, anchorPoint.y)
    var elements: Collection<Node<T>>? = null
        private set
    private var rectangle = Rectangle()
        get() = anchorPoint.makeRectangleWith(movingPoint)

    /**
     * Updates the box, clearing the old one and drawing the updated one.
     * @param newPoint the cursor's new position
     */
    fun update(newPoint: Point) {
        clear()
        movingPoint.x = newPoint.x
        movingPoint.y = newPoint.y
        draw()
    }

    /**
     * Cancels the current selection
     */
    fun cancel() {
        clear()
    }

    /**
     * Locks the elements and writes the items selected to [elements].
     * @param nodes a map having nodes as keys and their positions as values
     */
    fun finalize(nodes: Map<Node<T>, Position2D<*>>): Collection<Node<T>> =
        rectangle.let { selection ->
            nodes.filterValues { selection.contains(wormhole.getViewPoint(it)) }
                .keys
        }.also { elements = it }.also { clear() }

    /**
     * Draws the box.
     */
    private fun draw() {
        rectangle.let {
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
    private fun clear() {
        rectangle.let {
            Platform.runLater {
                context.clearRect(it.x, it.y, it.width, it.height)
            }
        }
    }
}

private fun Rectangle.contains(point: Point): Boolean =
    point.x in this.x..(this.x + this.width) &&
    point.y in this.y..(this.y + this.height)

private fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
    min(this.x, other.x).toDouble(),
    min(this.y, other.y).toDouble(),
    abs(this.x - other.x).toDouble(),
    abs(this.y - other.y).toDouble())
