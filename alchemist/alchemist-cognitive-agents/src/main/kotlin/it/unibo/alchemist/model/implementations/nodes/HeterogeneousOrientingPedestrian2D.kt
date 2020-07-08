/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Compliance
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.HelpAttitude
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 *
 */
open class HeterogeneousOrientingPedestrian2D<T, N : ConvexPolygon, E> @JvmOverloads constructor(
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double,
    group: PedestrianGroup2D<T>? = null,
    final override val age: Age,
    final override val gender: Gender
) : HomogeneousOrientingPedestrian2D<T, N, E>(
    environment,
    randomGenerator,
    knowledgeDegree = knowledgeDegree,
    group = group
), HeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation> {

    override val compliance: Double = Compliance(age, gender).level

    private val helpAttitude = HelpAttitude(age, gender)

    override fun probabilityOfHelping(
        toHelp: HeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation>
    ): Double = helpAttitude.level(toHelp.age, toHelp.gender, membershipGroup.contains(toHelp))

    /**
     * Allows to specify age and gender with a string.
     */
    @JvmOverloads constructor(
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
        randomGenerator: RandomGenerator,
        knowledgeDegree: Double,
        group: PedestrianGroup2D<T>? = null,
        age: String,
        gender: String
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromString(age),
        Gender.fromString(gender)
    )

    /**
     * Allows to specify age with an int and gender with a string.
     */
    @JvmOverloads constructor(
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
        randomGenerator: RandomGenerator,
        knowledgeDegree: Double,
        group: PedestrianGroup2D<T>? = null,
        age: Int,
        gender: String
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromYears(age),
        Gender.fromString(gender)
    )
}
