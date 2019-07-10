package it.unibo.alchemist.model.implementations.geometry

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2D
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape2DFactory
import org.apache.commons.math3.util.FastMath.toDegrees
import java.awt.geom.Arc2D
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

internal class AwtGeometricShape2DFactory : GeometricShape2DFactory<Euclidean2DPosition> {
    private val punctiformShape: GeometricShape2D<Euclidean2DPosition> = PunctiformShape(Euclidean2DPosition(0.0, 0.0))

    override fun punctiform(): GeometricShape2D<Euclidean2DPosition> = punctiformShape

    override fun rectangle(width: Double, height: Double): GeometricShape2D<Euclidean2DPosition> =
        AwtGeometricShape2D(Rectangle2D.Double(-width / 2, -height / 2, width, height))

    override fun circleSector(radius: Double, angle: Double, heading: Double): GeometricShape2D<Euclidean2DPosition> {
        val startAngle = heading - angle / 2
        return AwtGeometricShape2D(Arc2D.Double(-radius, -radius, radius * 2, radius * 2, toDegrees(startAngle), toDegrees(angle), Arc2D.PIE))
    }

    override fun circle(radius: Double): GeometricShape2D<Euclidean2DPosition> =
        AwtGeometricShape2D(Ellipse2D.Double(-radius, -radius, radius * 2, radius * 2))
}
