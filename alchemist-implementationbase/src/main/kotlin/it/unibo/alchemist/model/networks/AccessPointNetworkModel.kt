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

/**
 * An [ObservingRangeBasedNetworkModel] whose links depend on which nodes are access points, namely the nodes containing
 * [accessPointId].
 *
 * @param T the concentration type
 * @param P the position type
 * @param Input the state of a node that links depend on
 * @param environment the environment whose nodes this model links
 * @param radius the maximum connection radius
 * @property accessPointId the molecule id used to mark access-point nodes
 */
abstract class AccessPointNetworkModel<T, P : Position<P>, Input>(
    environment: Environment<T, P>,
    radius: Double,
    val accessPointId: Molecule,
) : ObservingRangeBasedNetworkModel<T, P, Input>(environment, radius) {

    /**
     * Whether this node is an access point.
     */
    protected val Node<T>.isAccessPoint: Boolean
        get() = contains(accessPointId)
}
