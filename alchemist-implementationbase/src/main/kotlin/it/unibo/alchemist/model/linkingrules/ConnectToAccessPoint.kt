/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.neighborhoods.Neighborhoods

/**
 * @param accessPointId the id of the access point.
 */
class ConnectToAccessPoint<T, P : Position<P>>(
    radius: Double,
    val accessPointId: Molecule,
) : ConnectWithinDistance<T, P>(radius) {

    private val Node<T>.isAccessPoint
        get() = contains(accessPointId)

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>): Neighborhood<T> =
        super.computeNeighborhood(center, environment).run {
            if (center.isAccessPoint) this else Neighborhoods.make(environment, center, filter { it.isAccessPoint })
        }
}
