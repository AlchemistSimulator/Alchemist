/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.steering

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment

/**
 * Weighted steering strategy where each steering action's weight is the inverse
 * of the node's distance to the action's target. Closer targets yield higher weight.
 * Actions without a target receive [defaultWeight].
 *
 * @param T the concentration type.
 * @param environment the environment in which the node moves.
 * @param node the owner of the steering action this strategy belongs to.
 * @param defaultWeight default weight for steering actions without a defined target.
 */
class DistanceWeighted<T>(
    environment: Euclidean2DEnvironment<T>,
    node: Node<T>,
    private val defaultWeight: Double = 1.0,
) : Weighted<T>(
    environment,
    node,
    {
        if (this is SteeringActionWithTarget) {
            targetDistanceTo(node, environment).let { if (it > 0.0) 1 / it else it }
        } else {
            defaultWeight
        }
    },
)
