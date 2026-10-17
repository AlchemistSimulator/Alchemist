/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * Represents a specific point on the Earth's surface.
 */
interface GeoPosition : Position2D<GeoPosition> {
    /**
     * The latitude of this position.
     */
    val latitude: Double

    /**
     * The longitude of this position.
     */
    val longitude: Double

    /**
     * Subtracts [other] from this [GeoPosition].
     */
    operator fun minus(other: GeoPosition): GeoPosition

    /**
     * Adds [other] to this [GeoPosition].
     */
    operator fun plus(other: GeoPosition): GeoPosition
}
