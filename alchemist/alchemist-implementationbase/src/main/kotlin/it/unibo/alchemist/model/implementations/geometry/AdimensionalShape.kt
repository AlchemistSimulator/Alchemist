package it.unibo.alchemist.model.implementations.geometry

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A special shape which does not occupy space and does not intersect with any other, not even with itself.
 * It also ignores any transformation.
 */
class AdimensionalShape<S : Vector<S>, A : GeometricTransformation<S>>(
    override val centroid: S
) : GeometricShape<S, A> {

    // TODO: spotbugs incorrectly reports this warning
    @SuppressFBWarnings("UWF_NULL_FIELD")
    override val diameter: Double = 0.0

    override fun intersects(other: GeometricShape<S, A>) = false

    override fun contains(vector: S) = false

    /**
     * Any transformation is ignored
     */
    override fun transformed(transformation: A.() -> Unit) = this
}