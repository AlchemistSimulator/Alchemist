/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.pedestrians

import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D
import org.dyn4j.dynamics.BodyFixture
import org.dyn4j.geometry.Circle
import org.dyn4j.geometry.Convex

/**
 *
 */
class ComfortAreaFixture<T>(
    val nodePhysics: PhysicalPedestrian2D<T>,
    shape: Convex = Circle(nodePhysics.comfortArea.radius),
) : BodyFixture(shape) {

    init {
        isSensor = true
    }
}
