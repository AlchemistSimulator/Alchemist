/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory

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
