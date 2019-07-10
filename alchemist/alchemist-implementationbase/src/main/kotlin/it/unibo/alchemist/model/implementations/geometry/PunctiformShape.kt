package it.unibo.alchemist.model.implementations.geometry

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2D

internal class PunctiformShape<P : Position2D<P>>(
    override val centroid: P
) : GeometricShape2D<P> {

    // TODO: spotbugs incorrectly reports this warning
    @SuppressFBWarnings("UWF_NULL_FIELD")
    override val diameter: Double = 0.0

    override fun withOrigin(position: P) = this

    override fun rotate(radians: Double) = this

    override fun contains(point: P) = false

    override fun intersects(other: GeometricShape2D<P>) = false
}