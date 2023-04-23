package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.Shape
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import it.unibo.alchemist.model.implementations.geometry.AbstractShapeFactory
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.util.FastMath.toDegrees
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

internal class AwtEuclidean2DShapeFactory(
    override val origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0),
) : AbstractShapeFactory<Euclidean2DPosition, Euclidean2DTransformation>(),
    Euclidean2DShapeFactory {

    @Suppress("UNCHECKED_CAST")
    override fun requireCompatible(
        shape: Shape<*, *>,
    ): Shape<Euclidean2DPosition, Euclidean2DTransformation> {
        require(shape is AwtEuclidean2DShape || shape is AdimensionalShape) {
            """"
                The given shape of type ${shape::class.simpleName} is not compatible with this environment, 
                to avoid this make sure to use PhysicsEnvironment.shapeFactory to create shapes
            """.trimIndent()
        }
        return shape as Shape<Euclidean2DPosition, Euclidean2DTransformation>
    }

    override fun rectangle(width: Double, height: Double): Euclidean2DShape =
        AwtEuclidean2DShape(Rectangle2D.Double(-width / 2, -height / 2, width, height))

    override fun circleSector(radius: Double, angle: Double, heading: Double): Euclidean2DShape {
        val startAngle = -heading - angle / 2
        return AwtEuclidean2DShape(
            Arc2D.Double(
                -radius,
                -radius,
                radius * 2,
                radius * 2,
                toDegrees(startAngle),
                toDegrees(angle),
                Arc2D.PIE,
            ),
        )
    }

    override fun circle(radius: Double): Euclidean2DShape =
        AwtEuclidean2DShape(Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2))
}
