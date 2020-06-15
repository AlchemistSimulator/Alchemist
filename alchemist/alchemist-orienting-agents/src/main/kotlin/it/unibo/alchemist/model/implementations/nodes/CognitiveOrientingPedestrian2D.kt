/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Age
import it.unibo.alchemist.model.cognitiveagents.characteristics.individual.Gender
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.HeterogeneousPedestrian
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive [OrientingPedestrian] in the Euclidean world.
 *
 * @param T the concentration type.
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param F the type of edges of the navigation graph provided by the environment.
 */
open class CognitiveOrientingPedestrian2D<T, M : ConvexPolygon, F> @JvmOverloads constructor(
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double,
    group: PedestrianGroup2D<T>? = null,
    final override val age: Age,
    final override val gender: Gender,
    danger: Molecule? = null
) : HomogeneousOrientingPedestrian2D<T, M, F>(
    environment,
    randomGenerator,
    knowledgeDegree = knowledgeDegree,
    group = group
), CognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation> {

    /**
     * Allows to specify age and gender with a string.
     */
    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
        group: PedestrianGroup2D<T>? = null,
        age: String,
        gender: String,
        danger: Molecule? = null
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromString(age),
        Gender.fromString(gender),
        danger
    )

    /**
     * Allows to specify age with an int and gender with a string.
     */
    @JvmOverloads constructor(
        knowledgeDegree: Double,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, M, F>,
        group: PedestrianGroup2D<T>? = null,
        age: Int,
        gender: String,
        danger: Molecule? = null
    ) : this(
        environment,
        randomGenerator,
        knowledgeDegree,
        group,
        Age.fromYears(age),
        Gender.fromString(gender),
        danger
    )

    /*
     * The cognitive part of the pedestrian.
     */
    private val cognitive = CognitivePedestrian2D(environment, randomGenerator, age, gender, danger, group)

    override val compliance = cognitive.compliance

    override fun probabilityOfHelping(
        toHelp: HeterogeneousPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation>
    ) = cognitive.probabilityOfHelping(toHelp)

    override fun dangerBelief() = cognitive.dangerBelief()

    override fun fear() = cognitive.fear()

    override fun wantsToEvacuate() = cognitive.wantsToEvacuate()

    override fun cognitiveCharacteristics() = cognitive.cognitiveCharacteristics()

    override fun influencialPeople() = cognitive.influencialPeople()
}
