/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy
import kotlin.math.cos
import kotlin.math.sin

/**
 * It's an [AbstractConfigurableMoveNode] for [Euclidean2DPosition] which provides a default [interpolatePositions]
 * that is accurate with respect to the target given and the current maximum speed.
 */
abstract class AbstractEuclidean2DConfigurableMoveNode<T>(
    environment: Environment<T, Euclidean2DPosition>,
    node: Node<T>,
    routing: RoutingStrategy<Euclidean2DPosition>,
    target: TargetSelectionStrategy<Euclidean2DPosition>,
    speed: SpeedSelectionStrategy<Euclidean2DPosition>
) : AbstractConfigurableMoveNode<T, Euclidean2DPosition>(environment, node, routing, target, speed) {

    /**
     * If [maxWalk] is greater than the speed needed to reach [target] then it positions precisely on [target]
     * without going farther.
     */
    override fun interpolatePositions(
        current: Euclidean2DPosition,
        target: Euclidean2DPosition,
        maxWalk: Double
    ): Euclidean2DPosition = with(target - current) {
        if (distanceTo(current) < maxWalk) {
            this
        } else {
            val angle = this.asAngle
            environment.makePosition(maxWalk * cos(angle), maxWalk * sin(angle))
        }
    }
}
