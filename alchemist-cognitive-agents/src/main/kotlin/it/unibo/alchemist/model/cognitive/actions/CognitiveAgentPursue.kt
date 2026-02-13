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
import it.unibo.alchemist.model.cognitive.navigation.Pursue
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.geometry.navigationgraph.Euclidean2DPassage

/**
 * A cognitive navigation action that pursues a static destination using the [Pursue] strategy.
 *
 * @param T the concentration type
 * @param L the landmark shape type used by the node's cognitive map
 * @param R the relation/edge type used by the node's cognitive map
 * @param environment the environment hosting the node
 * @param reaction the reaction executing this action
 * @param pedestrian the owner pedestrian property
 * @param destination vararg coordinates describing the destination position
 */
class CognitiveAgentPursue<T, L : Euclidean2DConvexShape, R>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: PedestrianProperty<T>,
    vararg destination: Number,
) : CognitiveAgentNavigationAction2D<T, L, R>(environment, reaction, pedestrian) {
    init {
        strategy = Pursue(this, environment.makePosition(*destination))
    }
}
