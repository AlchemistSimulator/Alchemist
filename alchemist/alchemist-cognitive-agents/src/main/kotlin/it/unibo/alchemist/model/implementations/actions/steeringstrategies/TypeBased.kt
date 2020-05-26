package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.SteeringAction
import kotlin.reflect.KClass

/**
 * Steering strategy which gives a weight based on the type of steering action you are considering.
 * For each type,
 * only the action with the target nearest to the current pedestrian position is taken into consideration.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 * @param typeWeights
 *          the weight for each type of steering action.
 */
class TypeBased<T>(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian<T>,
    typeWeights: LinkedHashMap<KClass<SteeringAction<T, Euclidean2DPosition>>, Double>,
    defaultWeight: Double = 0.0
) : Weighted<T>(env, pedestrian, { typeWeights[this::class] ?: defaultWeight })
