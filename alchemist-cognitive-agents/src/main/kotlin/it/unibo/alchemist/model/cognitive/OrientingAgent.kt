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
 * @param V the [Vector] type for the space this agent is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks. See [cognitiveMap].
 * @param R the type of edges of the [cognitiveMap], representing the [R]elations between landmarks.
 */
interface OrientingAgent<V, A, L, R> where
          V : Vector<V>,
          A : Transformation<V>,
          L : ConvexShape<V, A> {
    /**
     * The knowledge degree of the agent concerning the environment. This is a Double value in [0, 1] describing the
     * percentage of environment the agent is familiar with prior to the start of the simulation (thus it does not
     * take into account the knowledge the node will gain during it, namely the [volatileMemory]).
     */
    val knowledgeDegree: Double

    /**
     * The cognitive map of the agent. It's a graph composed of landmarks (elements of the environment easy to
     * remember due to their uniqueness) and spatial relations between them. It's modeled as a [NavigationGraph].
     */
    val cognitiveMap: NavigationGraph<V, A, L, R>

    /**
     * The volatile memory of the agent: it models the ability to remember areas of the environment already visited
     * since the start of the simulation. Each area is paired with the number of visits. Areas are assumed to be
     * represented as [ConvexShape]s, as in [NavigationGraph]s.
     */
    val volatileMemory: MutableMap<ConvexShape<V, A>, Int>

    /**
     * Registers a visit to the provided [area] in the agent's [volatileMemory].
     */
    fun <M : ConvexShape<V, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }
}
