/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.actions

import it.unibo.alchemist.device.properties.UpdateStatusProperty
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.nextUp

class EvaluateSpawning<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    private val node: Node<T>,
    private val random: RandomGenerator,
    private val statusProperty: UpdateStatusProperty<T, P>,
) : AbstractAction<T>(node) {
    override fun cloneAction(
        node: Node<T>,
        reaction: Reaction<T>,
    ): Action<T> = EvaluateSpawning(environment, node, random, statusProperty)

    init {
        require(statusProperty.node == node) { "The node of the property must be the same as the node of the action" }
    }

    override fun execute() {
        val localPosition = environment.getPosition(node).coordinates
        val coordinate = localPosition.map { it + random.nextDouble(-1.0, 1.0) }
        val spawningTime = environment.simulation.time + DoubleTime(random.nextDouble(0.0.nextUp(), 0.1))
        val cloneOfThis = node.cloneNode(spawningTime)
        val updatedPosition = environment.makePosition(*coordinate.toList().toTypedArray())
        environment.addNode(cloneOfThis, updatedPosition)
    }

    override fun getContext(): Context? = Context.NEIGHBORHOOD
}
