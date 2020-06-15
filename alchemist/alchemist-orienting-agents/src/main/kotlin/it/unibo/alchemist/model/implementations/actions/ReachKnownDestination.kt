/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.navigationstrategies.KnownDestinationReaching
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.math.lazyMutable
import it.unibo.alchemist.model.interfaces.NavigationStrategy2D
import it.unibo.alchemist.model.interfaces.OrientingPedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage

/**
 * A [NavigationAction] using [KnownDestinationReaching] navigation strategy.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
class ReachKnownDestination<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian2D<T, N, E>,
    vararg destinations: Number
) : NavigationAction2DImpl<T, N, E>(environment, reaction, pedestrian) {

    override var strategy: NavigationStrategy2D<T, N, E, ConvexPolygon, Euclidean2DPassage> by lazyMutable {
        KnownDestinationReaching(this, destinations.toPositions(environment))
    }
}
