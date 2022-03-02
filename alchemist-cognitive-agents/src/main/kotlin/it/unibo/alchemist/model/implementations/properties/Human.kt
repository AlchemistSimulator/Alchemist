/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.HelpAttitude
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.properties.PedestrianIndividuality2DCapability
import org.apache.commons.math3.random.RandomGenerator

/**
 * A pedestrian's individual characteristics.
 */
class Human<T> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val age: Age,
    override val gender: Gender,
    override val speed: Speed = Speed(age, gender, randomGenerator),
    override val compliance: Double = Compliance(age, gender).level,
    override val helpAttitude: HelpAttitude = HelpAttitude(age, gender)
) : PedestrianIndividuality2DCapability<T>
