/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.nodes

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A node with a [shape].
 */
interface NodeWithShape<T, S : Vector<S>, A : GeometricTransformation<S>> : Node<T> {
    /**
     * The shape of the node.
     */
    val shape: GeometricShape<S, A>
}
