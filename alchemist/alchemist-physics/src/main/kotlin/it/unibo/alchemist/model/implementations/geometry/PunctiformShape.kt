package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.interfaces.GeometricShape
import it.unibo.alchemist.model.interfaces.Position
import javafx.geometry.Point2D
import java.awt.Shape
import java.awt.geom.Ellipse2D

internal class PunctiformShape<P : Position<P>>(
    override val centroid: P
) : GeometricShape<P> {

    override val diameter: Double = 0.0

    override fun asShape() = Ellipse2D.Double()

    override fun withOrigin(position: P) = this

    override fun rotate(radians: Double) = this

    override fun contains(point: P) = false

    override fun intersects(other: GeometricShape<P>) = false
}