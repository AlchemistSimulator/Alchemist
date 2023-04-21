/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods

/**
 * @param accessPointId the id of the access point.
 */
class ConnectViaAccessPoint<T, P : Position<P>>(
    radius: Double,
    val accessPointId: Molecule,
) : ConnectWithinDistance<T, P>(radius) {

    private val Node<T>.isAccessPoint
        get() = contains(accessPointId)

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
