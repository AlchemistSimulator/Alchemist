/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.steering.Nearest
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment

/**
 * Steering behavior that executes only the steering action targeting the nearest destination.
 *
 * @param T the concentration type.
 * @param environment the environment in which the pedestrian moves.
 * @property pedestrian the owner of this reaction.
 * @param timeDistribution the time distribution governing reaction execution.
 */
class PrioritySteering<T>(
    environment: Euclidean2DEnvironment<T>,
    pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
) : SteeringBehavior<T>(environment, pedestrian, timeDistribution, Nearest(environment, pedestrian.node))
