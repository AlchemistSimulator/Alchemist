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
import it.unibo.alchemist.model.Position
import org.danilopianini.util.SpatialIndex

/**
 * An [AbstractEnvironment] implementation that intentionally provides no spatial functionality.
 *
 * This environment is meant as a placeholder or sentinel implementation in contexts where an
 * [it.unibo.alchemist.model.Environment]
 * instance is required, but node insertion, movement, neighborhood computation, and position construction must be
 * disallowed.
 *
 * @param incarnation the [Incarnation] associated with this environment.
 * @param dimensions the number of spatial dimensions exposed by this environment.
 */
class EmptyEnvironment<T, P : Position<P>>(incarnation: Incarnation<T, P>, override val dimensions: Int = 2) :
    AbstractEnvironment<T, P>(
        incarnation,
        object : SpatialIndex<Node<T>> {
            override val dimensions: Int = dimensions
            override fun insert(element: Node<T>, vararg position: Double) = nope()
            override fun remove(element: Node<T>, vararg position: Double) = nope()
            override fun move(element: Node<T>, start: DoubleArray, end: DoubleArray) = nope()
            override fun query(vararg parallelotope: DoubleArray) = emptyList<Node<T>>()
        },
    ) {

    /**
     * This environment does not support node insertion positions.
     *
     * @throws UnsupportedOperationException always.
     */
    override fun computeActualInsertionPosition(node: Node<T>, originalPosition: P): P = nope()

    /**
     * This environment does not support node insertion.
     *
     * @throws UnsupportedOperationException always.
     */
    override fun nodeAdded(node: Node<T>, position: P, neighborhood: Neighborhood<T>) = nope()

    /**
     * The origin (all zeros) for each coordinate axis.
     *
     * This value is provided only to satisfy the [AbstractEnvironment] contract and does not imply that
     * spatial operations are supported.
     */
    override val offset: DoubleArray = DoubleArray(dimensions) { 0.0 }

    /**
     * The (unit) extent for each coordinate axis.
     *
     * This value is provided only to satisfy the [AbstractEnvironment] contract and does not imply that
     * spatial operations are supported.
     */
    override val size: DoubleArray = DoubleArray(dimensions) { 1.0 }

    /**
     * This environment cannot create positions.
     *
     * @throws UnsupportedOperationException always.
     */
    override fun makePosition(vararg coordinates: Number): P = nope()

    /**
     * This environment cannot create positions.
     *
     * @throws UnsupportedOperationException always.
     */
    override fun makePosition(coordinates: DoubleArray): P = nope()

    /**
     * This environment does not support node movement.
     *
     * @throws UnsupportedOperationException always.
     */
    override fun moveNodeToPosition(node: Node<T>, newPosition: P) = nope()

    private companion object {

        /**
         * Throws the failure used by all unsupported operations in [EmptyEnvironment].
         *
         * @return never returns normally.
         * @throws UnsupportedOperationException always.
         */
        private fun nope(): Nothing = throw UnsupportedOperationException(
            "The empty environment cannot generate positions, and does not support the insertion of nodes.",
        )
    }
}
