package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.GeometricShape
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.lang.UnsupportedOperationException

/**
 * {@link GeometricShape} delegated to java.awt.geom.
 */
internal class AwtGeometricShape2D(
    private val shape: Shape,
    private val origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0)
) : GeometricShape<Euclidean2DPosition> {

    override val diameter: Double by lazy {
        val rect = shape.bounds2D
        Euclidean2DPosition(rect.minX, rect.minY).getDistanceTo(Euclidean2DPosition(rect.maxX, rect.maxY))
    }

    override val centroid: Euclidean2DPosition by lazy {
        Euclidean2DPosition(shape.bounds2D.centerX, shape.bounds2D.centerY)
    }

    override fun asShape() = AffineTransform().createTransformedShape(shape)!!

    override fun contains(point: Euclidean2DPosition) =
        shape.contains(Point2D.Double(point.x, point.y))

    override fun intersects(other: GeometricShape<Euclidean2DPosition>) =
        when (other) {
            /*
             checking for other.shape.intersects(shape.bounds2D) means that every shape becomes a rectangle.
             not checking for it results in paradoxes like shape.intersects(other) != other.intersects(shape).
             The asymmetry is tolerated in favour of a half-good implementation.
             */
            is AwtGeometricShape2D -> shape.intersects(other.shape.bounds2D) // || other.shape.intersects(shape.bounds2D)
            is PunctiformShape -> false
            else -> throw UnsupportedOperationException("AwtGeometricShape2D only works with other AwtGeometricShape2D")
        }

    override fun withOrigin(position: Euclidean2DPosition): GeometricShape<Euclidean2DPosition> {
        val tr = AffineTransform()
        val offset = position - origin
        tr.translate(offset.x, offset.y)
        return AwtGeometricShape2D(tr.createTransformedShape(shape), position)
    }

    override fun rotate(radians: Double): GeometricShape<Euclidean2DPosition> {
        val tr = AffineTransform()
        tr.rotate(radians, origin.x, origin.y)
        return AwtGeometricShape2D(tr.createTransformedShape(shape), origin)
    }
}