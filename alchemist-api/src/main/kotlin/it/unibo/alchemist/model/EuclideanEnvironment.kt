/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.geometry.Vector

/**
 * An Euclidean space, where [Position]s [P] are valid [Vector]s,
 * supporting any concentration type [T].
 */
interface EuclideanEnvironment<T, P> : Environment<T, P> where P : Position<P>, P : Vector<P> {
    /**
     * Moves [node] by [displacement], relative to its current position, through [moveNodeTo].
     * Environments constraining movements should override [moveNodeTo] rather than this function,
     * so that their constraints apply to absolute movements as well.
     */
    fun moveNodeBy(node: Node<T>, displacement: P) = moveNodeTo(node, getCurrentPosition(node) + displacement)

    /**
     * Create a position corresponding to the origin of this environment.
     */
    val origin: P get() = makePosition(generateSequence { 0 }.take(dimensions).toList())
}
