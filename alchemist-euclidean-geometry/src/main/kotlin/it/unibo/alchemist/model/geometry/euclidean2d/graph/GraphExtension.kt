/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.euclidean2d.graph

import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath

/**
 * Checks whether a path exists between [source] and [sink].
 * [DijkstraShortestPath] is used instead of [org.jgrapht.alg.connectivity.ConnectivityInspector.pathExists],
 * because, in case of directed graph, the latter checks whether the given vertices lay in the same weakly
 * connected component, which is not the desired behavior.
 * As unweighted graphs have a default edge weight of 1.0, shortest path algorithms can always be applied
 * meaningfully.
 */
fun <V> Graph<V, *>.pathExists(source: V, sink: V): Boolean =
    DijkstraShortestPath.findPathBetween(this, source, sink) != null
