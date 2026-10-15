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
import it.unibo.alchemist.model.EnvironmentWithObstacles
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.observation.Observable

/**
 * Similar to [ConnectWithinDistance], but if the environment has obstacles, the links they cross are removed.
 *
 * @param T concentration type
 * @param P position type
 * @param environment the environment whose nodes this model links
 * @param radius connection range
 */
class ObstaclesBreakConnection<T, P>(environment: Environment<T, P>, radius: Double) :
    ObservingRangeBasedNetworkModel<T, P, P>(environment, radius)
    where P : Position<P>, P : Vector<P> {

    private val obstacles = environment as? EnvironmentWithObstacles<*, T, P>

    override fun isLinked(center: Node<T>, centerInput: P, neighbor: Node<T>, neighborInput: P): Boolean =
        obstacles?.intersectsObstacle(centerInput, neighborInput) != true

    override fun observeInput(node: Node<T>): Observable<P> = environment.positionOf(node)
}
