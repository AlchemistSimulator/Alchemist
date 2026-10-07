/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Route
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * A movement combining three strategies: the next target to reach, the route to follow, and the speed to move at.
 *
 * @param T concentration type
 * @param P position type
 * @property routingStrategy the strategy computing routes towards targets
 * @property targetSelectionStrategy the strategy selecting the next target
 * @property speedSelectionStrategy the strategy selecting how far the node walks at each step
 */
abstract class AbstractConfigurableMoveNode<T, P : Position<P>> @JvmOverloads protected constructor(
    environment: Environment<T, P>,
    reaction: NodeReaction<T>,
    protected val routingStrategy: RoutingStrategy<T, P>,
    protected val targetSelectionStrategy: TargetSelectionStrategy<T, P>,
    protected val speedSelectionStrategy: SpeedSelectionStrategy<T, P>,
    isAbsolute: Boolean = false,
) : AbstractMoveNode<T, P>(environment, reaction, isAbsolute) {
    /**
     * The route currently being followed, or `null` if none is.
     */
    protected var currentRoute: Route<P>? = null
        private set

    /**
     * The current target.
     */
    protected var targetPoint: P? = null

    private var currentStep = 0

    final override fun getNextPosition(): P {
        val previousEnd = targetPoint
        val end = targetSelectionStrategy.getTarget()
        targetPoint = end
        if (end != previousEnd) {
            resetRoute()
        }
        val maxWalk = speedSelectionStrategy.getNodeMovementLength(end)
        val currentPosition = environment.getCurrentPosition(targetNode)
        return if (currentPosition.distanceTo(end) <= maxWalk) {
            targetPoint = targetSelectionStrategy.getTarget()
            resetRoute()
            if (isAbsolute) end else end.minus(currentPosition.coordinates)
        } else {
            walkAlongRoute(currentPosition, end, maxWalk)
        }
    }

    /**
     * Follows the current route (computing it if needed) from [start] towards [end] for at most [maxWalk].
     */
    private fun walkAlongRoute(start: P, end: P, maxWalk: Double): P {
        val route = currentRoute ?: routingStrategy.computeRoute(start, end).also { currentRoute = it }
        var position = start
        var walkable = maxWalk
        var blockingPoint: P? = null
        while (blockingPoint == null && currentStep < route.size()) {
            val target = route.getPoint(currentStep)
            val toWalk = target.distanceTo(position)
            if (toWalk > walkable) {
                // The node can walk at most the remaining length towards the next point of the route.
                blockingPoint = target
            } else {
                currentStep++
                walkable -= toWalk
                position = target
            }
        }
        return if (blockingPoint != null) {
            interpolatePositions(position, blockingPoint, walkable)
        } else {
            // The whole route has been followed, or it was empty.
            resetRoute()
            interpolatePositions(position, end, walkable)
        }
    }

    /**
     * Computes the position reached when moving from [current] towards [target] for at most [maxWalk], in absolute
     * or relative coordinates depending on [isAbsolute].
     */
    protected abstract fun interpolatePositions(current: P, target: P, maxWalk: Double): P

    /**
     * Discards the current route, for instance because its target has been reached.
     */
    protected fun resetRoute() {
        currentRoute = null
        currentStep = 0
    }
}
