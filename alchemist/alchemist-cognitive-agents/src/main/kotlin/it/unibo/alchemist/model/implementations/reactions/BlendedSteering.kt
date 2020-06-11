package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

/**
 * Steering behavior which sums all the actions with a different weight based on their target distance.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class BlendedSteering<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: Pedestrian2D<T>,
    timeDistribution: TimeDistribution<T>
) : SteeringBehavior<T>(environment, pedestrian, timeDistribution, DistanceWeighted(environment, pedestrian))
