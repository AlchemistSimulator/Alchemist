/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.util.FastMath.toRadians

/**
 * Spins the node owning this action around itself at [angularSpeedDegrees], normalized according to the rate of
 * its [reaction].
 */
class Spin<T>(
    reaction: NodeReaction<T>,
    private val environment: Physics2DEnvironment<T>,
    private val angularSpeedDegrees: Double,
) : AbstractLocalAction<T>(reaction) {
    private val angularSpeedRadians = toRadians(angularSpeedDegrees)
    private val timeDistributedReaction = requireNotNull(reaction as? TimeDistributedReaction<*>) {
        "$reaction does not expose a recurrence rate"
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>) = Spin(newReaction, environment, angularSpeedDegrees)

    /**
     * Spins the node around itself.
     */
    override fun execute() {
        val realSpeed = angularSpeedRadians / timeDistributedReaction.rate
        val headingAngle = environment.getHeading(targetNode).asAngle + realSpeed
        environment.setHeading(targetNode, environment.makePosition(cos(headingAngle), sin(headingAngle)))
    }
}
