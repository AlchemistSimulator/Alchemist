/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.actions.AbstractConfigurableMoveNode
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * It's an [AbstractConfigurableMoveNode] in the Euclidean world, which provides a default [interpolatePositions]
 * that is accurate with respect to the target given and the current maximum walking distance.
 */
abstract class AbstractEuclideanConfigurableMoveNode<T, P>(
    environment: Environment<T, P>,
    node: Node<T>,
    routing: RoutingStrategy<T, P>,
    target: TargetSelectionStrategy<T, P>,
    speed: SpeedSelectionStrategy<T, P>,
) : AbstractConfigurableMoveNode<T, P>(environment, node, routing, target, speed)
    where P : Position<P>, P : Vector<P> {

    /**
     * @returns the next relative position reached by the node. If [maxWalk] is greater than the distance to
     * the [target], the node positions precisely on [target] without going farther.
     */
    override fun interpolatePositions(current: P, target: P, maxWalk: Double): P =
        (target - current).coerceAtMost(maxWalk)
}
