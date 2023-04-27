/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions.steeringstrategies

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.reflect.KClass

/**
 * A [Weighted] steering strategy assigning weights based on actions' types (each type has its own weight, specified
 * by the client).
 *
 * @param environment
 *          the environment in which the node moves.
 * @param node
 *          the owner of the steering actions combined by this strategy.
 * @param typeWeights
 *          the weight for each type of steering action.
 */
class TypeBased<T>(
    environment: Euclidean2DEnvironment<T>,
    node: Node<T>,
    typeWeights: LinkedHashMap<KClass<SteeringAction<T, Euclidean2DPosition>>, Double>,
    defaultWeight: Double = 0.0,
) : Weighted<T>(environment, node, { typeWeights[this::class] ?: defaultWeight })
