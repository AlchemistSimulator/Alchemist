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
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D

/**
 * [Euclidean2DShape] delegated to java.awt.geom.
 */
internal class AwtEuclidean2DShape(
    private val shape: java.awt.Shape,
    private val origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0),
) : Euclidean2DShape, AwtShapeCompatible {

    override val diameter: Double by lazy {
        val rect = shape.bounds2D
        Euclidean2DPosition(rect.minX, rect.minY).distanceTo(Euclidean2DPosition(rect.maxX, rect.maxY))
    }

    override val centroid: Euclidean2DPosition by lazy {
        Euclidean2DPosition(shape.bounds2D.centerX, shape.bounds2D.centerY)
    }

    override fun transformed(transformation: Euclidean2DTransformation.() -> Unit) =
        with(MyTransformation()) {
            transformation.invoke(this)
            apply()
        }

    override fun asAwtShape() = AffineTransform().createTransformedShape(shape)!!

    /**
     * Delegated to [java.awt.Shape.contains], hence adopting the definition of insideness used by [java.awt.Shape]s.
     */
    override fun contains(vector: Euclidean2DPosition) =
        shape.contains(Point2D.Double(vector.x, vector.y))

    /**
     * Bounding boxes are used, allowing some inaccuracy.
     */
    override fun intersects(other: Euclidean2DShape) =
        when (other) {
            /*
             checking for other.shape.intersects(shape.bounds2D) means that every shape becomes a rectangle.
             not checking for it results in paradoxes like shape.intersects(other) != other.intersects(shape).
             The asymmetry is tolerated in favour of a half-good implementation.
             */
            is AwtEuclidean2DShape -> shape.intersects(other.shape.bounds2D)
            // || other.shape.intersects(shape.bounds2D)
            is AdimensionalShape -> false
            else -> throw UnsupportedOperationException("AwtEuclidean2DShape only works with other AwtEuclidean2DShape")
        }

    private inner class MyTransformation : Euclidean2DTransformation {
        private val transform = AffineTransform()
        private var newOrigin = origin
        private var newRotation = 0.0

        override fun origin(position: Euclidean2DPosition) {
            newOrigin = position
        }

        override fun rotate(angle: Double) {
            newRotation += angle
        }

        fun apply(): AwtEuclidean2DShape {
            transform.translate(newOrigin.x, newOrigin.y)
            if (newRotation != 0.0) {
                transform.rotate(newRotation)
            }
            transform.translate(-origin.x, -origin.y)
            return AwtEuclidean2DShape(
                transform.createTransformedShape(shape),
                newOrigin,
            )
        }
    }
}
