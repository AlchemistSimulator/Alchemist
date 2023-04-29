/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.NavigationStrategy2D
import it.unibo.alchemist.model.cognitive.OrientingProperty
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.navigation.ReachDestination
import it.unibo.alchemist.model.euclidean.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.util.Arrays.toPositions
import it.unibo.alchemist.util.lazyMutable
import org.jgrapht.Graphs

/**
 * A [CognitiveAgentNavigationAction2D] using [ReachDestination] navigation strategy.
 * Accepts an array of coordinates representing the destinations and uses [inferIsKnown] to partition them into
 * known and unknown ones.
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
class CognitiveAgentReachDestination<T, L : Euclidean2DConvexShape, R>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    vararg destinations: Number,
) : CognitiveAgentNavigationAction2D<T, L, R>(environment, reaction, pedestrian) {

    /**
     * Infers if a [destination] is known by the [navigatingNode]
     * (see [it.unibo.alchemist.model.cognitive.navigation.Pursue]). A destination is considered
     * to be known if the node's cognitive map contains at least one landmark located in the same
     * room (= [environment]'s area) of the destination, or in an adjacent room.
     */
    private fun inferIsKnown(destination: Euclidean2DPosition): Boolean =
        environment.graph.nodeContaining(destination)?.let { room ->
            val neighborhood = Graphs.neighborListOf(environment.graph, room) + room
            navigatingNode.asProperty<T, OrientingProperty<T, *, *, L, *, R>>()
                .cognitiveMap
                .vertexSet()
                .any { landmark -> neighborhood.any { it.contains(landmark.centroid) } }
        } ?: false

    override var strategy: NavigationStrategy2D<T, L, R, ConvexPolygon, Euclidean2DPassage> by lazyMutable {
        destinations
            .toPositions(environment)
            .partition { inferIsKnown(it) }
            .let { (known, unknown) -> ReachDestination(this, known, unknown) }
    }
}
