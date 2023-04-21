/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.util.FastMath.toRadians
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spins a [node] around itself at [angularSpeedDegrees] normalized according to the speed of the [reaction].
 */
class Spin<T>(
    node: Node<T>,
    private val reaction: Reaction<T>,
    private val environment: Physics2DEnvironment<T>,
    private val angularSpeedDegrees: Double,
) : AbstractAction<T>(node) {

    private val angularSpeedRadians = toRadians(angularSpeedDegrees)

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>) =
        Spin(node, reaction, environment, angularSpeedDegrees)

    /**
     * Spins the node around itself.
     */
    override fun execute() {
        val realSpeed = angularSpeedRadians / reaction.timeDistribution.rate
        val headingAngle = environment.getHeading(node).asAngle + realSpeed
        environment.setHeading(node, environment.makePosition(cos(headingAngle), sin(headingAngle)))
    }

    override fun getContext() = Context.LOCAL
}
