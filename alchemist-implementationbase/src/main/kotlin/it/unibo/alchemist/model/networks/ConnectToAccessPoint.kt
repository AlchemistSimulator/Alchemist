/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observation.Observable

/**
 * Network model that connects nodes within a specified radius, but connects a node that is not an access point only
 * to access points.
 *
 * @param T the concentration type
 * @param P the position type
 * @param environment the environment whose nodes this model links
 * @param radius the maximum connection radius
 * @param accessPointId the molecule id used to mark access-point nodes
 */
class ConnectToAccessPoint<T, P : Position<P>>(
    environment: Environment<T, P>,
    radius: Double,
    accessPointId: Molecule,
) : AccessPointNetworkModel<T, P, Boolean>(environment, radius, accessPointId) {

    override fun isLinked(center: Node<T>, centerInput: Boolean, neighbor: Node<T>, neighborInput: Boolean): Boolean =
        centerInput || neighborInput

    // Whether a node is an access point.
    override fun observeInput(node: Node<T>): Observable<Boolean> = node.observeContains(accessPointId)
}
