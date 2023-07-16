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
 * Defines the possible [Transformation]s for a GeometricShape in a
 * bidimensional Euclidean space.
 */
interface Euclidean2DTransformation : Transformation<Euclidean2DPosition> {

    /**
     * Counterclockwise rotation.
     *
     * @param angle the angle in radians
     */
    fun rotate(angle: Double)

    /**
     * Rotates toward the specified direction.
     *
     * @param direction the direction vector
     */
    fun rotate(direction: Euclidean2DPosition) = rotate(direction.asAngle)

    /**
     * Changes origin.
     */
    fun origin(x: Double, y: Double) = origin(Euclidean2DPosition(x, y))
}
