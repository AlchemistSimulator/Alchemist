package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.actions.steeringstrategies.Nearest
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * Steering behavior using [Nearest] steering strategy (= the only action executed is the one with the nearest target).
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class PrioritySteering<T>(
    environment: Euclidean2DEnvironment<T>,
    override val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
) : SteeringBehavior<T>(environment, pedestrian, timeDistribution, Nearest(environment, pedestrian.node))
