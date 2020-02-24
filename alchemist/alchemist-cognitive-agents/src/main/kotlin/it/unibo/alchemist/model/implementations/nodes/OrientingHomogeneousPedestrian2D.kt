/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import org.apache.commons.math3.random.RandomGenerator

/**
 * An orienting homogeneous pedestrian in an euclidean bidimensional space.
 *
 * @param E the type of edges of the environment's graph.
 */
class OrientingHomogeneousPedestrian2D<T, E : GraphEdge<ConvexPolygon>> @JvmOverloads constructor(
    knowledgeDegree: Double,
    rg: RandomGenerator,
    envGraph: NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, E>,
    env: EuclideanPhysics2DEnvironment<T>,
    group: PedestrianGroup<T>? = null
) : AbstractOrientingPedestrian2D<T, E>(knowledgeDegree, rg, envGraph, env, group) {

    private val shape = shape(env)

    init {
        senses += fieldOfView(env)
    }

    /**
     */
    override fun getShape() = shape
}
