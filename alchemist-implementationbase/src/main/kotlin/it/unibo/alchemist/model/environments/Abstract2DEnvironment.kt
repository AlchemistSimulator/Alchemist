/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import kotlin.math.max
import kotlin.math.nextDown
import kotlin.math.nextUp
import org.danilopianini.util.FlexibleQuadTree

/**
 * Models a bidimensional environment.
 *
 * @param T concentration type
 * @param P [Position2D] type
 * @param incarnation the incarnation to be used.
 */
abstract class Abstract2DEnvironment<T, P : Position2D<P>> protected constructor(incarnation: Incarnation<T, P>) :
    AbstractEnvironment<T, P>(incarnation, FlexibleQuadTree()) {

    final override val dimensions: Int = 2

    final override val offset: DoubleArray
        get() = doubleArrayOf(if (minX <= maxX) minX else Double.NaN, if (minY <= maxY) minY else Double.NaN)

    final override val size: DoubleArray get() = doubleArrayOf(max(0.0, maxX - minX), max(0.0, maxY - minY))

    private var maxX = Double.NEGATIVE_INFINITY

    private var maxY = Double.NEGATIVE_INFINITY

    private var minX = Double.POSITIVE_INFINITY

    private var minY = Double.POSITIVE_INFINITY

    /**
     * Movements are unconstrained: subclasses constraining them override this function, starting from the position
     * that it returns.
     */
    override fun farthestReachablePosition(node: Node<T>, desired: P): P = desired

    /**
     * Moves [node] to the [farthestReachablePosition] towards [position], extending the environment to include it.
     */
    override fun moveNodeTo(node: Node<T>, position: P) {
        val reachable = farthestReachablePosition(node, position)
        includeObject(reachable)
        setPosition(node, reachable)
    }

    /**
     * Subclasses can override this function to modify the actual position a node gets inserted in (e.g., to restrict
     * the areas in which a node can be).
     */
    override fun computeActualInsertionPosition(node: Node<T>, originalPosition: P): P = originalPosition

    /**
     * Extends the size of the environment to include an object spanning from [startx] to [endx] horizontally, and
     * from [starty] to [endy] vertically.
     */
    protected fun includeObject(startx: Double, endx: Double, starty: Double, endy: Double) {
        if (startx < minX) {
            minX = startx.nextDown()
        }
        if (starty < minY) {
            minY = starty.nextDown()
        }
        if (endx > maxX) {
            maxX = endx.nextUp()
        }
        if (endy > maxY) {
            maxY = endy.nextUp()
        }
    }

    /**
     * Extends the size of the environment to include [position].
     */
    protected fun includeObject(position: P) = includeObject(position.x, position.x, position.y, position.y)

    /**
     * Subclasses may override this function to hook to the node addition event. Overriders should call the super
     * implementation, as it ensures the environment bounds are updated considering the newly included object.
     */
    override fun nodeAdded(node: Node<T>, position: P, neighborhood: Neighborhood<T>) {
        includeObject(position)
    }
}
