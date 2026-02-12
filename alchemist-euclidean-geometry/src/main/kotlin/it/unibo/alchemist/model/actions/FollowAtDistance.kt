/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.movestrategies.speed.GloballyConstantSpeed
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Anys.toPosition
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Causes a node to follow a destination read from a [Molecule], maintaining a fixed [distance]
 * and respecting a maximum [speed].
 *
 * The destination is read from the specified [target] molecule and interpreted as coordinates or a tuple.
 *
 * @param T the concentration type.
 * @param node the follower node.
 * @param reaction the reaction hosting this action.
 * @param environment the environment containing the nodes.
 * @param target the molecule carrying the destination coordinates.
 * @param distance the distance to keep from the destination.
 * @param speed the maximum movement speed.
 */
class FollowAtDistance<T>(
    node: Node<T>,
    private val reaction: Reaction<T>,
    private val environment: Environment<T, Euclidean2DPosition>,
    private val target: Molecule,
    private val distance: Double,
    private val speed: Double,
) : AbstractAction<T>(node) {
    private val speedStrategy = GloballyConstantSpeed<T, Euclidean2DPosition>(reaction, speed)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) = FollowAtDistance(
        node,
        reaction,
        environment,
        target,
        distance,
        speed,
    )

    override fun execute() {
        node.getConcentration(target)?.also {
            val targetPosition = it.toPosition(environment)
            val currentPosition = environment.getPosition(node)
            var destination = targetPosition.surroundingPointAt(currentPosition - targetPosition, distance)
            if (currentPosition != destination) { // avoid "bouncing"
                val currentSpeed =
                    min(
                        speedStrategy.getNodeMovementLength(destination),
                        currentPosition.distanceTo(destination),
                    )
                val direction = destination - currentPosition
                val angle = direction.asAngle
                destination = currentPosition +
                    Euclidean2DPosition(currentSpeed * cos(angle), currentSpeed * sin(angle))
                environment.moveNodeToPosition(node, destination)
            }
        }
    }

    override fun getContext() = Context.LOCAL
}
