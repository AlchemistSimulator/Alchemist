/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanEnvironment

/**
 * A bidimensional euclidean space with any concentration type [T].
 */
interface Euclidean2DEnvironment<T> : EuclideanEnvironment<T, Euclidean2DPosition> {

    override val origin: Euclidean2DPosition get() = Companion.origin

    /**
     * Creates a new [Euclidean2DPosition].
     */
    fun makePosition(x: Double, y: Double): Euclidean2DPosition = Euclidean2DPosition(x, y)

    /**
     * Creates a new [Euclidean2DPosition].
     */
    override fun makePosition(vararg coordinates: Double): Euclidean2DPosition =
        if (coordinates.size == 2) makePosition(coordinates[0], coordinates[1])
        else throw IllegalArgumentException("Illegal coordinates (required 2): ${coordinates.contentToString()}")

    companion object {
        /**
         * The origin of this Euclidean environment: vector [0, 0].
         */
        val origin = Euclidean2DPosition(0.0, 0.0)
    }
}
