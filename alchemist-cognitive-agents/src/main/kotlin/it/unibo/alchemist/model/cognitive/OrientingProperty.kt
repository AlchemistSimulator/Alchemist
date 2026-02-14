/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.geometry.navigationgraph.NavigationGraph

/**
 * A node's orienting capability.
 *
 * @param T the concentration type.
 * @param P the [Position] and [Vector] type used by the environment.
 * @param A the transformation type supported by shapes in this environment.
 * @param L the type of landmarks in the cognitive map.
 * @param N the type of navigation-area shapes used to derive landmarks.
 * @param E the type of edges in the navigation graph.
 */
interface OrientingProperty<T, P, A, L, N, E> :
    NodeProperty<T>
    where P : Position<P>,
          P : Vector<P>,
          A : Transformation<P>,
          L : ConvexShape<P, A>,
          N : ConvexShape<P, A> {
    /**
     * The degree of prior knowledge about the environment, in [0.0, 1.0].
     */
    val knowledgeDegree: Double

    /** The environment in which the node moves. */
    val environment: EnvironmentWithGraph<*, T, P, A, N, E>

    /**
     * The agent's cognitive map: a [NavigationGraph] of landmarks and spatial relations.
     */
    val cognitiveMap: NavigationGraph<P, A, L, E>

    /**
     * Volatile memory tracking visited areas paired with the number of visits.
     */
    val volatileMemory: MutableMap<ConvexShape<P, A>, Int>

    /**
     * Registers a visit to the provided [area] in the volatile memory.
     *
     * @param area the area that has been visited.
     */
    fun <M : ConvexShape<P, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }

    /**
     * Creates a landmark entirely contained in the given [area]. If [area] contains one or more destinations,
     * the returned landmark must contain at least one of them.
     *
     * @param area the navigation-area used to create the landmark.
     * @return a landmark of type [L] contained in the provided area.
     */
    fun createLandmarkIn(area: N): L
}
