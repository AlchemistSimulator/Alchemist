package it.unibo.alchemist.model.implementations.actions.steeringstrategies

import it.unibo.alchemist.model.interfaces.*
import kotlin.reflect.KClass

/**
 * Steering strategy which gives a weight based on the type of steering action you are considering.
 * For each type, only the action with the target nearest to the current pedestrian position is taken into consideration.
 *
 * @param env
 *          the environment in which the pedestrian moves.
 * @param pedestrian
 *          the owner of the steering action this strategy belongs to.
 * @param typeWeights
 *          the weight for each type of steering action.
 */
class TypeBased<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    typeWeights: LinkedHashMap<KClass<SteeringAction<T, P>>, Double>
) : Weighted<T, P>(env, pedestrian, { typeWeights[this::class] ?: 0.0 })