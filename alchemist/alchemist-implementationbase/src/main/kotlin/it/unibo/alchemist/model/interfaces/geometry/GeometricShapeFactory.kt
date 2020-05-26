package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.implementations.geometry.AdimensionalShape
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.AwtEuclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShapeFactory
import java.io.Serializable

/**
 * Generic factory for [GeometricShape].
 */
interface GeometricShapeFactory<S : Vector<S>, A : GeometricTransformation<S>> : Serializable {

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
    fun requireCompatible(shape: GeometricShape<*, *>): GeometricShape<S, A>

    companion object {
        /**
         * Retrieves a factory of [GeometricShape] compatible with the given vector type.
         *
         * @param <S> The type of vector used
         * @param <A> The supported geometric transformations
         * @param <F> The interface of the factory requested
         * @return the factory
         */
        inline fun <reified S, reified A, reified F> getInstance(): F
            where S : Vector<S>,
                  A : GeometricTransformation<S>,
                  F : GeometricShapeFactory<S, A> =
            getInstance(F::class.java)

        /**
         * Retrieves a factory of [GeometricShape] compatible with the given space.
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
        fun <S, A, F> getInstance(type: Class<F>): F
            where S : Vector<S>,
                  A : GeometricTransformation<S>,
                  F : GeometricShapeFactory<S, A> =
            when (type) {
                Euclidean2DShapeFactory::class.java -> AwtEuclidean2DShapeFactory()
                else -> TODO("No implementation found for GeometricShapeFactory<" + type.simpleName + ">")
            } as F
    }
}
