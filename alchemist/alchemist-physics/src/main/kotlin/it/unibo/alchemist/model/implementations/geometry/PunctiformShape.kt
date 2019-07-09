package it.unibo.alchemist.model.implementations.geometry

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.interfaces.GeometricShape
import it.unibo.alchemist.model.interfaces.Position

internal class PunctiformShape<P : Position<P>>(
    override val centroid: P
) : GeometricShape<P> {

    // TODO: spotbugs incorrectly reports this warning
    @SuppressFBWarnings("UWF_NULL_FIELD")
    override val diameter: Double = 0.0

    override fun withOrigin(position: P) = this

    override fun rotate(radians: Double) = this

    override fun contains(point: P) = false

    override fun intersects(other: GeometricShape<P>) = false
}