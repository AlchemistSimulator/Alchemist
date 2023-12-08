/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape

/**
 * Base class for [Transformation] providing a standard implementation for
 * [GeometricShapeFactory.adimensional].
 */
abstract class AbstractShapeFactory<S : Vector<S>, A : Transformation<S>> : GeometricShapeFactory<S, A> {
    /**
     * The default origin for the shapes.
     */
    protected abstract val origin: S

    override fun adimensional() = AdimensionalShape<S, A>(origin)
}
