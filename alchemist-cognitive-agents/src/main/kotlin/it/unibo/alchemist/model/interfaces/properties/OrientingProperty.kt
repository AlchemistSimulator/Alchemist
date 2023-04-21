/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph

/**
 * A node's capability to orient.
 */
interface OrientingProperty<T, P, A, L, N, E> : NodeProperty<T>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {
    /**
     * The knowledge degree of the agent concerning the environment. This is a Double value in [0, 1] describing the
     * percentage of environment the agent is familiar with prior to the start of the simulation (thus it does not
     * take into account the knowledge the pedestrian will gain during it, namely the [volatileMemory]).
     */
    val knowledgeDegree: Double

    /**
     * The environment in which the node moves.
     */
    val environment: EnvironmentWithGraph<*, T, P, A, N, E>

    /**
     * The cognitive map of the agent. It's a graph composed of landmarks (elements of the environment easy to
     * remember due to their uniqueness) and spatial relations between them. It's modeled as a [NavigationGraph].
     */
    val cognitiveMap: NavigationGraph<P, A, L, E>

    /**
     * The volatile memory of the agent: it models the ability to remember areas of the environment already visited
     * since the start of the simulation. Each area is paired with the number of visits. Areas are assumed to be
     * represented as [ConvexGeometricShape]s, as in [NavigationGraph]s.
     */
    val volatileMemory: MutableMap<ConvexGeometricShape<P, A>, Int>

    /**
     * Registers a visit to the provided [area] in the agent's [volatileMemory].
     */
    fun <M : ConvexGeometricShape<P, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }

    /**
     * Creates a landmark entirely contained in the given area. If such area contains one or more destinations, the
     * returned landmark must contain at least one of them.
     */
    fun createLandmarkIn(area: N): L
}
