package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.implementations.geometry.AwtGeometricShape2DFactory
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * Factory for {@link GeometricShape}.
 */
interface GeometricShape2DFactory<P : Position2D<P>> {

    /**
     * A circle extends in the first and second axis by its diameter.
     *
     * @param radius the radius
     * @return the shape
     */
    fun circle(radius: Double): GeometricShape<P>

    /**
     * A rectangle extends in the first and second axis by its width and height.
     *
     * @param width the width
     * @param height the height
     * @return the shape
     */
    fun rectangle(width: Double, height: Double): GeometricShape<P>

    /**
     * A circle sector is the portion of a disk enclosed by two radii and an arc
     * and it extends in the first and second axis by its radius and angle.
     *
     *
     * @param radius the radius of the circle from which the sector is extracted
     * @param angle the angle of the arc in radians, it determines its dimension in the second axis
     * @param heading the angle in radians of the median segment which bisects the sector.
     *                  It's used to determine the sector's heading.
     * @return the shape
     */
    fun circleSector(radius: Double, angle: Double, heading: Double): GeometricShape<P>

    /**
     * A punctiform shape occupies no space.
     * @return the shape
     */
    fun punctiform(): GeometricShape<P>

    companion object {
        /**
         * Retrieves a factory of {@link GeometricShape} compatible with the given {@link Position} type.
         * Meant for java compatibility.
         *
         * @param <P> The type of the {@link Position}
         * @param type The type of the {@link Position} wanted
         * @return the factory
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <P : Position2D<P>> getInstance(type: Class<P>): GeometricShape2DFactory<P> =
            when (type) {
                Euclidean2DPosition::class.java -> AwtGeometricShape2DFactory()
                else -> throw NotImplementedError("GeometricShape2DFactory<" + type.simpleName + "> isn't implemented yet")
            } as GeometricShape2DFactory<P>

        /**
         * Retrieves a factory of {@link GeometricShape} compatible with the given {@link Position} type.
         *
         * @param <P> The type of the {@link Position}
         * @return the factory
         */
        inline fun <reified P : Position2D<P>> getInstance(): GeometricShape2DFactory<P> =
            getInstance(P::class.java)
    }
}


