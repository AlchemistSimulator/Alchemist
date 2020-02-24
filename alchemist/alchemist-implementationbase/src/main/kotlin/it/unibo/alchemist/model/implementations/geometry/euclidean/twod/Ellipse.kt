package it.unibo.alchemist.model.implementations.geometry.euclidean.twod

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexEuclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import java.awt.geom.Ellipse2D

/**
 * Adaptor of [java.awt.geom.Ellipse2D] to [ConvexEuclidean2DShape].
 */
class Ellipse(
    private val ellipse: Ellipse2D
) : ConvexEuclidean2DShape, AwtShapeCompatible {

    private var euclideanShape = AwtEuclidean2DShape(ellipse)

    override val centroid = euclideanShape.centroid

    override val diameter = euclideanShape.diameter

    override fun intersects(other: Euclidean2DShape) = euclideanShape.intersects(other)

    override fun contains(vector: Euclidean2DPosition) = euclideanShape.contains(vector)

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) =
        euclideanShape.transformed(transformation) as Euclidean2DShape

    override fun asAwtShape() = ellipse
}
