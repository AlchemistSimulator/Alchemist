package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D

/**
 * {@link GeometricShape} delegated to java.awt.geom.
 */
internal class AwtGeometricShape2D(
    private val shape: Shape,
    private val origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0)
) : Euclidean2DShape, AwtShapeCompatible {

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit): Euclidean2DShape {
        val t = MyTransformation()
        transformation.invoke(t)
        return t.execute()
    }

    override val diameter: Double by lazy {
        val rect = shape.bounds2D
        Euclidean2DPosition(rect.minX, rect.minY).getDistanceTo(Euclidean2DPosition(rect.maxX, rect.maxY))
    }

    override val centroid: Euclidean2DPosition by lazy {
        Euclidean2DPosition(shape.bounds2D.centerX, shape.bounds2D.centerY)
    }

    override fun asAwtShape() = AffineTransform().createTransformedShape(shape)!!

    override fun contains(point: Euclidean2DPosition) =
        shape.contains(Point2D.Double(point.x, point.y))

    override fun intersects(other: Euclidean2DShape) =
        when (other) {
            /*
             checking for other.shape.intersects(shape.bounds2D) means that every shape becomes a rectangle.
             not checking for it results in paradoxes like shape.intersects(other) != other.intersects(shape).
             The asymmetry is tolerated in favour of a half-good implementation.
             */
            is AwtGeometricShape2D -> shape.intersects(other.shape.bounds2D) // || other.shape.intersects(shape.bounds2D)
            is AdimensionalShape -> false
            else -> throw UnsupportedOperationException("AwtGeometricShape2D only works with other AwtGeometricShape2D")
        }

    private inner class MyTransformation : Euclidean2DTransformation {
        private val transform = AffineTransform()

        override fun origin(position: Euclidean2DPosition) {
            val offset = position - origin
            transform.translate(offset.x, offset.y)
        }

        override fun rotate(angle: Double) {
            transform.rotate(angle, origin.x, origin.y)
        }

        fun execute() = AwtGeometricShape2D(transform.createTransformedShape(shape))
    }
}