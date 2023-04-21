package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.geometry.GeometricShape
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A special shape which does not occupy space and does not intersect with any other, not even with itself.
 * It also ignores any transformation.
 */
class AdimensionalShape<S : Vector<S>, A : GeometricTransformation<S>>(
    override val centroid: S,
) : GeometricShape<S, A> {

    override val diameter: Double = 0.0

    override fun intersects(other: GeometricShape<S, A>) = false

    override fun contains(vector: S) = false

    /**
     * Any transformation is ignored.
     */
    override fun transformed(transformation: A.() -> Unit) = this
}
