/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.graph.twod

import it.unibo.alchemist.model.implementations.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph

/**
 * Defines a connection between two [ConvexPolygon]s represented as an [Euclidean2DSegment].
 * The segment models the shape of the passage between the two areas: an agent could cross
 * any point of it to move from one room (or better, polygon) to the other. To make it easier,
 * think of the following: in an indoor environment the segment should represent the shape of
 * the door between two rooms. Additionally, given a crossing c connecting convex polygon a
 * to convex polygon b, the segment provided by c MUST belong to the boundary of a, but can
 * or cannot belong the boundary of b.
 */
typealias Euclidean2DCrossing = GraphEdgeWithData<ConvexPolygon, Segment2D<Euclidean2DPosition>>

/**
 * A navigation graph in an euclidean bidimensional environment, whose
 * nodes are [ConvexPolygon]s and edges are [Euclidean2DCrossing]s.
 */
typealias Euclidean2DNavigationGraph =
    NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>

/**
 * Builder for [Euclidean2DNavigationGraph].
 */
typealias Euclidean2DNavigationGraphBuilder =
    NavigationGraphBuilder<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>
