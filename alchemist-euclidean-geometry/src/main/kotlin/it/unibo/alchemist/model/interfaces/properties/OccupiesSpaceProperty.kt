/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.geometry.GeometricShape
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A node's ability to have a [Shape].
 */
interface OccupiesSpaceProperty<T, S : Vector<S>, A : GeometricTransformation<S>> : NodeProperty<T> {
    /**
     * The node's shape.
     */
    val shape: GeometricShape<S, A>
}
