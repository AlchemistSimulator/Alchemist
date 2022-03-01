/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianMovementCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianRunningCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianWalkingCapability
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator

/**
 * Implementation of a basic [PedestrianMovementCapability].
 */
open class BasePedestrianMovementCapability<T> @JvmOverloads constructor(
    /**
     * The simulation random generator.
     */
    private val randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val walkingSpeed: Double = Speed.default,
    override val runningSpeed: Double = Speed.default * 3,
) : PedestrianMovementCapability<T>,
    PedestrianWalkingCapability<T> by BasePedestrianWalkingCapability(node, walkingSpeed),
    PedestrianRunningCapability<T> by BasePedestrianRunningCapability(node, runningSpeed) {
    override fun speed(): Double = randomGenerator.nextDouble(walkingSpeed, runningSpeed)
}
