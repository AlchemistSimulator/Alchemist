package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.Nearest
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * Steering behavior where only the action whose target is the nearest to the current pedestrian position is executed.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class PrioritySteering<T>(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>
) : SteeringBehavior<T>(env, pedestrian, timeDistribution, Nearest(env, pedestrian))
