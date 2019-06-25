package it.unibo.alchemist.model.influencesphere.shapes

import it.unibo.alchemist.model.interfaces.Position2D
import java.awt.Shape

/**
 * Defines a generic 2D shape.
 */
open class GeometricShape2D<P : Position2D<P>>(shape: Shape) : GeometricShape<P>, Shape by shape {

    override fun contains(point: P): Boolean = contains(point.x, point.y)
}