/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.AwtShapeCompatible
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.geom.Ellipse2D

/**
 * Adapter of [java.awt.geom.Ellipse2D] to [Euclidean2DConvexShape].
 */
class Ellipse(
    private val ellipse: Ellipse2D,
) : Euclidean2DConvexShape, AwtShapeCompatible {

    private val euclideanShape = AwtEuclidean2DShape(ellipse)

    override val centroid = euclideanShape.centroid

    override val diameter = euclideanShape.diameter

    override fun intersects(other: Euclidean2DShape) = euclideanShape.intersects(other)

    override fun contains(vector: Euclidean2DPosition) = euclideanShape.contains(vector)

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) =
        euclideanShape.transformed(transformation) as Euclidean2DShape

    override fun asAwtShape() = ellipse
}
