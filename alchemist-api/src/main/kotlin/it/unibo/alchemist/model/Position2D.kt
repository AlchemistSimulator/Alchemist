/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model

/**
 * A bidimensional position.
 *
 * @param <P>
</P> */
interface Position2D<P : Position2D<P>> : Position<P> {

    @Deprecated("Access to coordinates in a 2D manifold should be performed using getX / getY")
    override fun getCoordinate(dimension: Int): Double

    /**
     * @return horizontal position
     */
    val x: Double

    /**
     * @return vertical position
     */
    val y: Double
}
