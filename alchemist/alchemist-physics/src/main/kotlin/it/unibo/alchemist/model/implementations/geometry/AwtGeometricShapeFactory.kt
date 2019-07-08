package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.GeometricShape
import it.unibo.alchemist.model.interfaces.GeometricShapeFactory
import org.apache.commons.math3.util.FastMath.toDegrees
import java.awt.geom.*

internal class AwtGeometricShapeFactory : GeometricShapeFactory<Euclidean2DPosition> {
    private val punctiformShape: GeometricShape<Euclidean2DPosition> = PunctiformShape(Euclidean2DPosition(0.0, 0.0))

    override fun punctiform(): GeometricShape<Euclidean2DPosition> = punctiformShape

    override fun rectangle(width: Double, height: Double): GeometricShape<Euclidean2DPosition> =
        AwtGeometricShape2D(Rectangle2D.Double(-width / 2, -height / 2, width, height))

    override fun circleSector(radius: Double, angle: Double, heading: Double): GeometricShape<Euclidean2DPosition> {
        val startAngle = heading - angle / 2
        return AwtGeometricShape2D(Arc2D.Double(-radius, -radius, radius * 2, radius * 2, toDegrees(startAngle), toDegrees(angle), Arc2D.PIE))
    }

    override fun circle(radius: Double): GeometricShape<Euclidean2DPosition> =
        AwtGeometricShape2D(Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2))
}
