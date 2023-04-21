/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

/**
 * Defines a generic transformation of a generic shape.
 * The operations allowed depend on the space the shape belongs to.
 * This interface is meant to be extended.
 */
interface GeometricTransformation<S : Vector<S>> {

    /**
     * Performs an absolute translation to the provided position.
     * @param position the new origin of the shape
     */
    fun origin(position: S)
}
