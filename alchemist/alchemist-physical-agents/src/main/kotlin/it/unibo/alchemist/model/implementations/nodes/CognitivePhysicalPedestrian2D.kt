/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.ComfortRepulsionNode2D
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive pedestrian capable of physical interactions, modeled as a [ComfortRepulsionNode2D]. [comfortRay] changes
 * dynamically depending on whether the pedestrian wants to evacuate or not.
 */
open class CognitivePhysicalPedestrian2D<T> @JvmOverloads constructor(
    override val environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    age: String,
    gender: String,
    danger: Molecule? = null,
    group: PedestrianGroup2D<T>? = null
) : CognitivePedestrian2D<T>(environment, randomGenerator, age, gender, danger, group), ComfortRepulsionNode2D<T> {

    /**
     * TODO(maybe introduce some randomness? To model differences in people)
     */
    override val comfortRay: Double get() =
        if (wantsToEvacuate()) {
            shape.radius / 3
        } else {
            shape.radius
        }
}
