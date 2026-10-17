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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position

/**
 * [it.unibo.alchemist.model.NetworkModel] which connects the nodes of [environment] whose distance is shorter than
 * [range].
 *
 * @param T concentration type
 * @param P position type
 * @param environment the environment whose nodes this model links
 * @param range the connection radius
 */
class ConnectWithinDistance<T, P : Position<P>>(environment: Environment<T, P>, range: Double) :
    RangeBasedNetworkModel<T, P>(environment, range) {
    override fun isLinked(center: Node<T>, neighbor: Node<T>): Boolean = true
}
