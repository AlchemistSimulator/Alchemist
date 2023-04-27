/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.navigation.GoalOrientedExploration
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.util.Arrays.toPositions

/**
 * A [CognitiveAgentNavigationAction2D] using [GoalOrientedExploration] navigation strategy.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
class CognitiveAgentGoalOrientedExplore<T, L : Euclidean2DConvexShape, R>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    vararg unknownDestinations: Number,
) : CognitiveAgentNavigationAction2D<T, L, R>(environment, reaction, pedestrian) {

    init {
        strategy = GoalOrientedExploration(this, unknownDestinations.toPositions(environment))
    }
}
