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
import it.unibo.alchemist.model.cognitive.NavigationStrategy2D
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.navigation.ReachKnownDestination
import it.unibo.alchemist.model.euclidean.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.util.Arrays.toPositions
import it.unibo.alchemist.util.lazyMutable

/**
 * A [CognitiveAgentNavigationAction2D] using [ReachKnownDestination] navigation strategy.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
class CognitiveAgentReachKnownDestination<T, L : Euclidean2DConvexShape, R>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    vararg destinations: Number,
) : CognitiveAgentNavigationAction2D<T, L, R>(environment, reaction, pedestrian) {

    override var strategy: NavigationStrategy2D<T, L, R, ConvexPolygon, Euclidean2DPassage> by lazyMutable {
        ReachKnownDestination(this, destinations.toPositions(environment))
    }
}
