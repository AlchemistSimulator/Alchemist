package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.implementations.geometry.AwtGeometricShapeFactory
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

/**
 * Factory for {@link GeometricShape}.
 */
interface GeometricShapeFactory<P : Position<P>> {

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
        fun <P : Position<P>> getInstance(type: Class<P>): GeometricShapeFactory<P> =
            when (type) {
                Euclidean2DPosition::class.java -> AwtGeometricShapeFactory()
                else -> throw NotImplementedError("GeometricShapeFactory<" + type.simpleName + "> isn't implemented yet")
            } as GeometricShapeFactory<P>

        /**
         * Retrieves a factory of {@link GeometricShape} compatible with the given {@link Position} type.
         *
         * @param <P> The type of the {@link Position}
         * @return the factory
         */
        inline fun <reified P : Position<P>> getInstance(): GeometricShapeFactory<P> =
            getInstance(P::class.java)
    }
}
