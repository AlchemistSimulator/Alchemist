/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An Euclidean space, where [Position]s [P] are valid [Vector]s,
 * supporting any concentration type [T].
 */
interface EuclideanEnvironment<T, P> : Environment<T, P>
where P : Position<P>, P : Vector<P> {

    /**
     * This method moves a [node] in the environment toward some [direction]. If
     * node move is unsupported, it does nothing.
     * Subclasses may override this method if they want to change the way a node
     * moves towards some direction. The current implementation internally calls
     * {@link #moveNodeToPosition(Node, Position2D)}, as such, overriding that
     * method may suffice.
     */
    fun moveNode(node: Node<T>, direction: P) {
        val oldcoord = getPosition(node)
        moveNodeToPosition(node, oldcoord.plus(direction))
    }

    /**
     * Creates a [Position] compatible with this environment given its [coordinates].
     */
    fun makePosition(vararg coordinates: Double): P

    /**
     * Create a position corresponding to the origin of this environment.
     */
    val origin: P get() = makePosition(*DoubleArray(dimensions).toTypedArray())
}
