/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.capabilities.BasePedestrian2DPhysicalCapability
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.PhysicalPedestrian2D
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.nextDouble
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.Pedestrian2DPhysicalCapability
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive pedestrian capable of physical interactions, modeled as a [PhysicalPedestrian2D]. [comfortRay] changes
 * dynamically depending on whether the pedestrian wants to evacuate or not.
 */
open class CognitivePhysicalPedestrian2D<T, S : Vector<S>, A : GeometricTransformation<S>> @JvmOverloads constructor(
    incarnation: Incarnation<T, Euclidean2DPosition>,
    randomGenerator: RandomGenerator,
    environment: Physics2DEnvironment<T>,
    nodeCreationParameter: String? = null,
    age: String,
    gender: String,
    danger: Molecule? = null,
    group: Group<T>? = null
) : CognitivePedestrian2D<T>(
    incarnation = incarnation,
    randomGenerator = randomGenerator,
    environment = environment,
    nodeCreationParameter = nodeCreationParameter,
    age = age,
    gender = gender,
    danger = danger,
    group = group,
),
    PhysicalPedestrian2D<T> {

    init {
        backingNode.addCapability(
            BasePedestrian2DPhysicalCapability(
                randomGenerator,
                backingNode,
                backingNode.asCapability<T, Spatial2DCapability<T, S, A>>().shape
            )
        )
    }

    /*
     *  According to [the work of Pelechano et al](https://bit.ly/3e3C7Tb) in order to bring out
     *  pushing behavior different nodes must have different personal space threshold.
     */
    private val desiredSpaceTreshold: Double = randomGenerator.nextDouble(minimumSpaceTreshold, maximumSpaceThreshold)

    override val comfortRay: Double = backingNode.asCapability<T, Pedestrian2DPhysicalCapability<T>>().comfortRay

    override val comfortArea = backingNode.asCapability<T, BasePedestrian2DPhysicalCapability<T>>().comfortArea
        get() = environment.getPosition(this).let { field.transformed { origin(it) } }

    companion object {
        /**
         * Mimimum value for normal state [comfortRay].
         */
        private const val minimumSpaceTreshold = 0.1
        /**
         * Maximum value for normal state [comfortRay].
         */
        private const val maximumSpaceThreshold = 1.0
    }
}
