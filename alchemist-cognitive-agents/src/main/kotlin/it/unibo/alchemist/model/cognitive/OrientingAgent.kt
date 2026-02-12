/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.geometry.navigationgraph.NavigationGraph

/**
 * An agent capable of orienting itself inside an environment.
 *
 * @param V the [Vector] type for the space this agent occupies.
 * @param A the transformation type supported by the shapes in this space.
 * @param L the type of landmarks stored in the cognitive map.
 * @param R the type of edges in the cognitive map, representing relations between landmarks.
 */
interface OrientingAgent<V, A, L, R> where
          V : Vector<V>,
          A : Transformation<V>,
          L : ConvexShape<V, A> {
    /**
     * The degree of prior knowledge about the environment, in [0.0, 1.0].
     * This value describes the portion of the environment the agent is familiar with before the simulation starts
     * and does not include memory gained during the simulation (see [volatileMemory]).
     */
    val knowledgeDegree: Double

    /**
     * The agent's cognitive map: a [NavigationGraph] of landmarks and spatial relations.
     */
    val cognitiveMap: NavigationGraph<V, A, L, R>

    /**
     * Volatile memory that tracks areas visited since the simulation started. The map pairs each visited area
     * with the number of visits.
     */
    val volatileMemory: MutableMap<ConvexShape<V, A>, Int>

    /**
     * Registers a visit to the provided [area] in the agent's volatile memory.
     *
     * @param area the area that has been visited.
     */
    fun <M : ConvexShape<V, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }
}
