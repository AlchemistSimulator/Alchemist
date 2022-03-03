/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.NodeProperty
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A node's ability to have a [Shape].
 */
interface OccupiesSpaceProperty<T, S : Vector<S>, A : GeometricTransformation<S>> : NodeProperty<T> {
    /**
     * The node's shape.
     */
    val shape: GeometricShape<S, A>
}

/**
 * A node's ability to occupy space in a 2D space.
 */
typealias AreaProperty<T> = OccupiesSpaceProperty<T, Euclidean2DPosition, Euclidean2DTransformation>
