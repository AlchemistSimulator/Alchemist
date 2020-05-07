package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.TimeDistribution

/**
 * Steering behavior which sums all the actions with a different weight based on their target distance.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class BlendedSteering<T>(
    env: Environment<T, Euclidean2DPosition>,
    pedestrian: Pedestrian<T>,
    timeDistribution: TimeDistribution<T>
) : SteeringBehavior<T>(env, pedestrian, timeDistribution, DistanceWeighted(env, pedestrian))
