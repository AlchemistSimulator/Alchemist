/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.cognitiveagents.impact.ImpactModel
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianCognitiveCapability
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianIndividualityCapability
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * The node's [CognitiveModel].
 */
class BasePedestrianCognitive2DCapability<T> @JvmOverloads constructor(
    /**
     * The environment in which the node moves.
     */
    environment: Physics2DEnvironment<T>,
    node: Node<T>,
    /**
     * The molecule associated with danger in the environment.
     */
    danger: Molecule? = null,
) : PedestrianCognitiveCapability<T> by BasePedestrianCognitiveCapability(
    environment,
    node,
    danger,
    {
        ImpactModel(
            node.asCapability<T, PedestrianIndividualityCapability<T, Euclidean2DPosition, Euclidean2DTransformation>>().compliance,
            { node.asCapability<T, PedestrianCognitiveCapability<T>>().influencialPeople() }
        ) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(node)) as Double }
                .orElse(0.0)
        }
    }
)
