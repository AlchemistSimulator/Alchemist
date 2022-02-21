/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.HeterogeneousPedestrianModel
import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.cognitiveagents.impact.ImpactModel
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Age
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Gender
import it.unibo.alchemist.model.cognitiveagents.impact.individual.Speed
import it.unibo.alchemist.model.implementations.capabilities.BasePedestrianCognitive2DCapability
import it.unibo.alchemist.model.implementations.capabilities.BasePedestrianIndividuality2DCapability
import it.unibo.alchemist.model.implementations.capabilities.BasePerceptionOfOthers2D
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.CognitivePedestrian
import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive [OrientingPedestrian] in the Euclidean world.
 *
 * @param T the concentration type.
 * @param N the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
class CognitiveOrientingPedestrian2D<T, N : ConvexPolygon, E> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    backingNode: Node<T>,
    knowledgeDegree: Double,
    group: Group<T>? = null,
    age: Age,
    gender: Gender,
    danger: Molecule? = null,
) : HomogeneousOrientingPedestrian2D<T, N, E>(
    randomGenerator,
    environment,
    backingNode,
    knowledgeDegree = knowledgeDegree,
    group = group
),
    CognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation> {

    /**
     * Allows to specify age and gender with a string.
     */
    @JvmOverloads constructor(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
        nodeCreationParameter: String? = null,
        knowledgeDegree: Double,
        group: Group<T>? = null,
        age: Any,
        gender: String,
        danger: Molecule? = null
    ) : this(
        randomGenerator,
        environment,
        incarnation.createNode(randomGenerator, environment, nodeCreationParameter),
        knowledgeDegree,
        group,
        Age.fromAny(age),
        Gender.fromString(gender),
        danger
    )

    /**
     * The pedestrian model, containing its age, gender, and speed.
     */
    val pedestrianModel: HeterogeneousPedestrianModel<T, Euclidean2DPosition, Euclidean2DTransformation> =
        HeterogeneousPedestrianModel(age = age, gender = gender, speed = Speed(age, gender, randomGenerator))

    /**
     * The [CognitiveModel] of the pedestrian, storing their mental state.
     */
    override val cognitiveModel: CognitiveModel by lazy {
        ImpactModel(pedestrianModel.compliance, ::influencialPeople) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(this)) as Double }
                .orElse(0.0)
        }
    }

    init {
        with(backingNode) {
            addCapability(BasePerceptionOfOthers2D(environment, backingNode))
            addCapability(
                BasePedestrianIndividuality2DCapability(
                    backingNode, age, gender, Speed(age, gender, randomGenerator)
                )
            )
            addCapability(BasePedestrianCognitive2DCapability(environment, backingNode, danger))
        }
    }
}
