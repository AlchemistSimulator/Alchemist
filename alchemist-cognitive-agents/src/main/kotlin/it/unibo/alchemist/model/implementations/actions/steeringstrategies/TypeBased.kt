package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import kotlin.reflect.KClass

/**
 * A [Weighted] steering strategy assigning weights based on actions' types (each type has its own weight, specified
 * by the client).
 *
 * @param environment
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering actions combined by this strategy.
 * @param typeWeights
 *          the weight for each type of steering action.
 */
class TypeBased<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: Node<T>,
    typeWeights: LinkedHashMap<KClass<SteeringAction<T, Euclidean2DPosition>>, Double>,
    defaultWeight: Double = 0.0
) : Weighted<T>(environment, pedestrian, { typeWeights[this::class] ?: defaultWeight })
