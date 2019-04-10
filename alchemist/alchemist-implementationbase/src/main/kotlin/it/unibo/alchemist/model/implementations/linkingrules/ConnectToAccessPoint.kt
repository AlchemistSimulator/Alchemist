/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position

class ConnectToAccessPoint<T, P : Position<P>>(radius: Double, val accessPointId: Molecule)
    : ConnectWithinDistance<T, P>(radius) {

    private val Node<T>.isAccessPoint
        get() = contains(accessPointId)

    override fun computeNeighborhood(center: Node<T>, env: Environment<T, P>): Neighborhood<T> =
        super.computeNeighborhood(center, env).run {
            if (center.isAccessPoint) this else Neighborhoods.make(env, center, filter { it.isAccessPoint })
        }
}
