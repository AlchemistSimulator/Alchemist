/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.navigationstrategies.Exploring
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.interfaces.OrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage

/**
 * A [NavigationAction] using [Exploring] navigation strategy.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map.
 */
class Explore<T, L : Euclidean2DConvexShape, R>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian2D<T, L, R>
) : NavigationAction2DImpl<T, L, R>(environment, reaction, pedestrian) {

    init {
        strategy = Exploring(this)
    }
}
