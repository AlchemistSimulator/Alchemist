/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.movestrategies.target

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.model.movestrategies.target.FollowTarget

/**
 * This strategy reads the value of a "target" molecule and tries to interpret it as a coordinate.
 *
 * @param T concentration type
 * @param environment the environment
 * @param node the node
 * @param targetMolecule the target molecule
 */
open class FollowTargetOnMap<T>(environment: Environment<T, GeoPosition>, node: Node<T>, targetMolecule: Molecule) :
    FollowTarget<T, GeoPosition>(environment, node, targetMolecule) {
    override fun cloneIfNeeded(destination: Node<T>, reaction: NodeReaction<T>): FollowTargetOnMap<T> =
        FollowTargetOnMap(environment, destination, targetMolecule)

    /**
     * Builds a [LatLongPosition] from the latitude [x] and the longitude [y].
     */
    override fun createPosition(x: Double, y: Double): GeoPosition = LatLongPosition(x, y)
}
