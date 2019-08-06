package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.TypeBased
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * A priority steering between actions of the same type which are then blended together.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class PriorityBlendedSteering<T, P : Position<P>>(
    env: Environment<T, P>,
    pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>
) : SteeringBehavior<T, P>(env, pedestrian, timeDistribution, TypeBased(env, pedestrian))