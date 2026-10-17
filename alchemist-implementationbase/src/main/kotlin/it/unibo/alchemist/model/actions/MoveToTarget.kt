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
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.movestrategies.RoutingStrategy
import it.unibo.alchemist.model.movestrategies.speed.ConstantSpeed
import it.unibo.alchemist.model.movestrategies.target.FollowTarget
import it.unibo.alchemist.model.routes.PolygonalChain
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Moves the node at [speed] towards the position stored as concentration of [trackMolecule].
 *
 * @param T concentration type
 * @param P position type
 */
class MoveToTarget<T, P : Position2D<P>>(
    environment: Environment<T, P>,
    reaction: NodeReaction<T>,
    private val trackMolecule: Molecule,
    private val speed: Double,
) : AbstractConfigurableMoveNode<T, P>(
    environment,
    reaction,
    RoutingStrategy { start, end -> PolygonalChain(start, end) },
    FollowTarget(environment, reaction.host, trackMolecule),
    ConstantSpeed(reaction, speed),
) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): MoveToTarget<T, P> =
        MoveToTarget(environment, newReaction, trackMolecule, speed)

    override fun interpolatePositions(current: P, target: P, maxWalk: Double): P {
        val vector = target.minus(current.coordinates)
        if (current.distanceTo(target) < maxWalk) {
            return vector
        }
        val angle = atan2(vector.y, vector.x)
        return environment.makePosition(maxWalk * cos(angle), maxWalk * sin(angle))
    }
}
