/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.navigationstrategies.DestinationReaching
import it.unibo.alchemist.model.implementations.actions.navigationstrategies.Pursuing
import it.unibo.alchemist.model.interfaces.NavigationAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.lazyMutable
import it.unibo.alchemist.model.interfaces.EuclideanNavigationStrategy
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import org.jgrapht.Graphs

/**
 * A [NavigationAction] using [DestinationReaching] navigation strategy.
 * Accepts an array of coordinates representing the destinations and uses [inferIsKnown] to partition them into
 * known and unknown ones.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
class ReachDestination<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    vararg destinations: Number
) : BaseEuclideanNavigationAction<T, N, E>(environment, reaction, pedestrian) {

    override var strategy: EuclideanNavigationStrategy<T, N, E, ConvexPolygon, Euclidean2DPassage> by lazyMutable {
        destinations
            .toPositions(environment)
            .partition { inferIsKnown(it, pedestrian, environment) }
            .let { (known, unknown) ->
                DestinationReaching(this, known, unknown)
            }
    }
}

/**
 * Infers if a [destination] is known by the [pedestrian] (see [Pursuing]). A destination is considered
 * to be known if the pedestrian's cognitive map contains at least one landmark located in the same
 * room (= [environment]'s area) of the destination, or in an adjacent room.
 */
fun <T, P, N : ConvexGeometricShape<P, *>, M : ConvexGeometricShape<P, *>> inferIsKnown(
    destination: P,
    pedestrian: OrientingPedestrian<T, P, *, N, *>,
    environment: EnvironmentWithGraph<*, T, P, *, M, *>
): Boolean where P : Position<P>, P : Vector<P> = environment.graph.nodeContaining(destination)?.let { room ->
    val neighborhood = Graphs.neighborListOf(environment.graph, room) + room
    pedestrian.cognitiveMap.vertexSet().any { landmark ->
        neighborhood.any { it.contains(landmark.centroid) }
    }
} ?: false
