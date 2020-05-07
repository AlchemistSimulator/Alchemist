/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph

/**
 * An agent capable of orienting itself inside an environment.
 *
 * @param V the [Vector] type for the space this agent is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks. See [cognitiveMap].
 * @param E the type of edges of the [cognitiveMap].
 */
interface OrientingAgent<V, A, N, E> where
    V : Vector<V>,
    A : GeometricTransformation<V>,
    N : ConvexGeometricShape<V, A> {

    /**
     * The knowledge degree of the agent concerning the environment, it's a Double
     * value in [0, 1] describing the percentage of the environment the agent is
     * familiar with prior to the start of the simulation (thus it does not take
     * into account the knowledge the pedestrian will gain during it, namely the
     * [volatileMemory]).
     */
    val knowledgeDegree: Double

    /**
     * The cognitive map of the agent. It's composed of landmarks (elements of the
     * environment easy to remember due to their uniqueness) and spatial relations
     * between them. It is represented using a [NavigationGraph].
     */
    val cognitiveMap: NavigationGraph<V, A, N, E>

    /**
     * The volatile memory of the agent: it models the ability to remember areas
     * of the environment already visited by the agent since the start of the
     * simulation. In particular, each area is paired with the number of visits.
     * Areas are assumed to be represented as [ConvexGeometricShape]s, as in
     * [NavigationGraph]s.
     */
    val volatileMemory: MutableMap<in ConvexGeometricShape<V, A>, Int>

    /**
     * Registers a visit to the provided [area] in the agent's [volatileMemory].
     */
    fun <M : ConvexGeometricShape<V, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }

    /**
     * Unregisters a visit to the provided [area] in the agent's [volatileMemory].
     */
    fun <M : ConvexGeometricShape<V, A>> unregisterVisit(area: M) {
        volatileMemory[area]?.let { volatileMemory[area] = it - 1 }
    }
}
