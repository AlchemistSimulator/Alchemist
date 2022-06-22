package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * Steering behavior using [DistanceWeighted] steering strategy (= steering actions are summed with different
 * weights depending on the distance to their target).
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param node
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
open class BlendedSteering<T>(
    environment: Euclidean2DEnvironment<T>,
    override val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>
) : SteeringBehavior<T>(environment, pedestrian, timeDistribution, DistanceWeighted(environment, pedestrian.node))
