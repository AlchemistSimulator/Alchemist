/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Defines a factory of [Shape] for a bidimensional euclidean space.
 */
interface Euclidean2DShapeFactory : GeometricShapeFactory<Euclidean2DPosition, Euclidean2DTransformation> {
    /**
     * A circle extends in the first and second axis by its diameter.
     *
     * @param radius the radius
     * @return the shape
     */
    fun circle(radius: Double): Euclidean2DShape

    /**
     * A rectangle extends in the first and second axis by its width and height.
     *
     * @param width the width
     * @param height the height
     * @return the shape
     */
    fun rectangle(width: Double, height: Double): Euclidean2DShape

    /**
     * A circle sector is the portion of a disk enclosed by two radii and an arc
     * and it extends in the first and second axis by its radius and angle.
     *
     *
     * @param radius the radius of the circle from which the sector is extracted
     * @param angle the angle of the arc in radians, it determines its dimension in the second axis
     * @param heading the angle in radians of the median segment which bisects the sector.
     *                  It's used to determine the sector's heading.
     * @return the shape
     */
    fun circleSector(radius: Double, angle: Double, heading: Double): Euclidean2DShape
}
