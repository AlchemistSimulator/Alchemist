/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.interfaces.properties.RunningPedestrianProperty
import it.unibo.alchemist.model.interfaces.properties.WalkingPedestrianProperty
import it.unibo.alchemist.model.util.RandomGeneratorExtension.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic [PedestrianProperty].
 */
open class Pedestrian<T> @JvmOverloads constructor(
    /**
     * The simulation random generator.
     */
    private val randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val walkingSpeed: Double = Speed.default,
    override val runningSpeed: Double = Speed.default * 3,
) : AbstractNodeProperty<T>(node),
    PedestrianProperty<T>,
    WalkingPedestrianProperty<T> by WalkingPedestrian(node, walkingSpeed),
    RunningPedestrianProperty<T> by RunningPedestrian(node, runningSpeed) {

    override fun speed(): Double = randomGenerator.nextDouble(walkingSpeed, runningSpeed)

    override fun cloneOnNewNode(node: Node<T>) = Pedestrian(randomGenerator, node, walkingSpeed, runningSpeed)

    override fun toString() = "${super.toString()}[walkingSpeed=$walkingSpeed, runningSpeed=$runningSpeed]"
}
