/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.euclidean.geometry

import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.geometry.Shape
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
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
