/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import java.io.Serializable

/**
 * Generic factory for [Shape] instances.
 *
 * @param S the [Vector] type used by the shapes created by this factory.
 * @param A the [Transformation] type supported by the shapes.
 */
interface GeometricShapeFactory<S : Vector<S>, A : Transformation<S>> : Serializable {
    /**
     * A special shape that occupies no space and does not intersect any other shape.
     */
    fun adimensional(): AdimensionalShape<S, A>

    /**
     * Requires that the given shape is compatible with those provided by this factory;
     * otherwise throws an exception.
     *
     * @param shape the shape to check.
     * @return the same shape if compatible.
     */
    fun requireCompatible(shape: Shape<*, *>): Shape<S, A>

    /** Factory methods for [Shape]. */
    companion object {
        /**
         * Retrieves a factory of [Shape] compatible with the given vector and transformation types.
         *
         * @param S The type of vector used.
         * @param A The supported geometric transformations.
         * @param F The factory interface requested.
         * @return an instance of the requested factory interface [F].
         */
        inline fun <S, A, reified F> getInstance(): F
            where S : Vector<S>,
                  A : Transformation<S>,
                  F : GeometricShapeFactory<S, A> =
            getInstance(F::class.java)

        /**
         * Retrieves a factory of [Shape] compatible with the given space (Java-friendly overload).
         *
         * @param S The type of vector used.
         * @param A The supported geometric transformations.
         * @param F The factory interface requested.
         * @param type the [Class] object representing the requested factory interface.
         * @return an instance of the requested factory interface [F].
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <S, A, F> getInstance(
            type: Class<F>,
        ): F
            where S : Vector<S>,
                  A : Transformation<S>,
                  F : GeometricShapeFactory<S, A> =
            when (type) {
                Euclidean2DShapeFactory::class.java -> AwtEuclidean2DShapeFactory()
                else -> TODO("No implementation found for GeometricShapeFactory<" + type.simpleName + ">")
            } as F
    }
}
