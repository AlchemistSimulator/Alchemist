/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.nextDouble

/**
 * A cognitive pedestrian capable of physical interactions, modeled as a [PhysicalPedestrian2D]. [comfortRay] changes
 * dynamically depending on whether the pedestrian wants to evacuate or not.
 */
open class CognitivePhysicalPedestrian2D<T> @JvmOverloads constructor(
    environment: Physics2DEnvironment<T>,
    randomGenerator: RandomGenerator,
    age: String,
    gender: String,
    danger: Molecule? = null,
    group: PedestrianGroup2D<T>? = null
) : CognitivePedestrian2D<T>(environment, randomGenerator, age, gender, danger, group), PhysicalPedestrian2D<T> {

    /*
     *  According to [the work of Pelechano et al](https://bit.ly/3e3C7Tb) in order to bring out
     *  pushing behavior different nodes must have different personal space threshold.
     */
    private val desiredSpaceTreshold: Double = randomGenerator.nextDouble(minimumSpaceTreshold, maximumSpaceThreshold)

    override val comfortRay: Double get() =
        if (wantsToEscape()) {
            desiredSpaceTreshold / 3
        } else {
            desiredSpaceTreshold
        }

    override val comfortArea = super.comfortArea
        get() = environment.getPosition(this)?.let { field.transformed { origin(it) } } ?: field

    companion object {
        /**
         * Mimimum value for normal state [comfortRay].
         */
        const val minimumSpaceTreshold = 0.1
        /**
         * Maximum value for normal state [comfortRay].
         */
        const val maximumSpaceThreshold = 1.0
    }
}
