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
import java.io.Serializable

/**
 * Generic factory for [Shape].
 */
interface GeometricShapeFactory<S : Vector<S>, A : Transformation<S>> : Serializable {
    /**
     * A special shape which does not occupy space and does not intersect with any other, not even with itself.
     */
    fun adimensional(): AdimensionalShape<S, A>

    /**
     * Requires that the given shape is compatible with the ones provided by this factory,
     * otherwise throws an exception.
     *
     * @param shape the shape to check
     * @return the same shape
     */
    fun requireCompatible(shape: Shape<*, *>): Shape<S, A>

    /**
     * Factory methods for [Shape].
     */
    companion object {
        /**
         * Retrieves a factory of [Shape] compatible with the given vector type.
         *
         * @param <S> The type of vector used
         * @param <A> The supported geometric transformations
         * @param <F> The interface of the factory requested
         * @return the factory
         */
        inline fun <S, A, reified F> getInstance(): F
            where S : Vector<S>,
                  A : Transformation<S>,
                  F : GeometricShapeFactory<S, A> =
            getInstance(F::class.java)

        /**
         * Retrieves a factory of [Shape] compatible with the given space.
         * (This method is meant for compatibility with java).
         *
         * @param <S> The type of vector used
         * @param <A> The supported geometric transformations
         * @param <F> The interface of the factory requested
         * @param type The interface of the factory requested
         * @return the factory
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
