/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.movestrategies.speed.GloballyConstantSpeed
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.util.AnyExtension.toPosition
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Makes the [Node] follow a [target] defined in a [Molecule] with some [speed],
 * but keeping a [distance] from it.
 *
 * @param <T> concentration type
 * @param environment the environment containing the nodes
 * @param node the follower
 * @param reaction the reaction hosting this action
 * @param target molecule from which to read the destination to follow in the form of coordinates or a tuple
 * @param distance the distance to keep from the destination
 * @param speed the maximum speed
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

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        FollowAtDistance(node, reaction, environment, target, distance, speed)

    override fun execute() {
        node.getConcentration(target)?.also {
            val targetPosition = it.toPosition(environment)
            val currentPosition = environment.getPosition(node)
            var destination = targetPosition.surroundingPointAt(currentPosition - targetPosition, distance)
            if (currentPosition != destination) { // avoid "bouncing"
                val currentSpeed = min(
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
