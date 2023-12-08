/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.navigationgraph

import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import org.jgrapht.GraphType
import org.jgrapht.graph.AbstractBaseGraph
import org.jgrapht.graph.DefaultGraphType
import org.jgrapht.util.SupplierUtil
import java.util.function.Supplier

/**
 * An implementation of [NavigationGraph], deriving from [AbstractBaseGraph].
 * The user can specify the [GraphType] so as to obtain a custom graph (e.g. weighted or not,
 * directed or undirected, etc). [AbstractBaseGraph] guarantees deterministic ordering for
 * the collection it maintains, as stated in its api documentation.
 * Note that vertices and edges are used as keys inside [AbstractBaseGraph], so when choosing
 * their types, you must follow these rules:
 * - You must follow the contract defined in java.lang.Object for both equals and hashCode.
 * - In particular, if you override either equals or hashCode, you must override them both.
 * - Your implementation for hashCode must produce a value which does not change over the
 * lifetime of the object.
 * Further information available [here](https://jgrapht.org/guide/VertexAndEdgeTypes).
 */
open class BaseNavigationGraph<V, A, N, E>(
    vertexSupplier: Supplier<N>?,
    edgeSupplier: Supplier<E>?,
    graphType: GraphType,
) : NavigationGraph<V, A, N, E>,
    AbstractBaseGraph<N, E>(vertexSupplier, edgeSupplier, graphType)
    where V : Vector<V>,
          A : Transformation<V>,
          N : ConvexShape<V, A> {

    /*
     * Allows to rapidly create a directed or undirected unweighted graph without
     * self-loops and allowing multiple edges.
     */
    constructor(edgeClass: Class<out E>, directed: Boolean) : this(
        null,
        SupplierUtil.createSupplier(edgeClass),
        DefaultGraphType.Builder().let {
            when {
                directed -> it.directed()
                else -> it.undirected()
            }
        }.weighted(false).allowMultipleEdges(true).allowSelfLoops(false).build(),
    )
}
