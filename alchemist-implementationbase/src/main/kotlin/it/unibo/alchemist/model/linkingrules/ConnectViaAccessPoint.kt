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
 * Connnects nodes within a given distance, but only if they are connected to the same access point,
 * emulating a classic NAT.
 *
 * @param accessPointId the [Molecule] identifying the access points.
 */
class ConnectViaAccessPoint<T, P : Position<P>>(
    radius: Double,
    accessPointId: Molecule,
) : AbstractAccessPointRule<T, P>(radius, accessPointId) {

    private fun Neighborhood<T>.closestAccessPoint(environment: Environment<T, P>): Node<T>? =
        asSequence().filter { it.isAccessPoint }.minByOrNull { environment.getDistanceBetweenNodes(center, it) }

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>): Neighborhood<T> =
        super.computeNeighborhood(center, environment).run {
            if (!center.isAccessPoint) {
                // Connect to closest access point and all nodes connected to the same AP
                closestAccessPoint(environment)?.let { closestAP ->
                    Neighborhoods.make(
                        environment,
                        center,
                        neighbors.filter {
                            it == closestAP || !it.isAccessPoint && environment.getNeighborhood(it).contains(closestAP)
                        },
                    )
                } ?: Neighborhoods.make(environment, center, emptyList())
            } else {
                if (all { it.isAccessPoint }) {
                    this
                } else {
                    // The AP must connect only if it is the closest AP of each node or the node is not connected
                    Neighborhoods.make(
                        environment,
                        center,
                        neighbors
                            .asSequence()
                            .filter { neighbor ->
                                neighbor.isAccessPoint ||
                                    environment.getNeighborhood(neighbor).run {
                                        contains(center) || none { it.isAccessPoint }
                                    }
                            }
                            .asIterable(),
                    )
                }
            }
        }
}
