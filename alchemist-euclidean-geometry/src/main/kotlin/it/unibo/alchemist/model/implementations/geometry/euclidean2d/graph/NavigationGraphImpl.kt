package it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph

import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DNavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph
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
          A : GeometricTransformation<V>,
          N : ConvexGeometricShape<V, A> {

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

/**
 * A directed unweighted [NavigationGraph], allowing multiple edges between the
 * same pair of vertices and without self-loops (i.e. edges connecting a node to
 * itself).
 */
class DirectedNavigationGraph<V, A, N, E>(
    edgeClass: Class<out E>,
) : BaseNavigationGraph<V, A, N, E>(edgeClass, true)
    where V : Vector<V>,
          A : GeometricTransformation<V>,
          N : ConvexGeometricShape<V, A>

/**
 * An undirected unweighted [NavigationGraph], allowing multiple edges between the
 * same pair of vertices and without self-loops (i.e. edges connecting a node to
 * itself).
 */
class UndirectedNavigationGraph<V, A, N, E>(
    edgeClass: Class<out E>,
) : BaseNavigationGraph<V, A, N, E>(edgeClass, false)
    where V : Vector<V>,
          A : GeometricTransformation<V>,
          N : ConvexGeometricShape<V, A>

/**
 * A directed [Euclidean2DNavigationGraph].
 */
typealias DirectedEuclidean2DNavigationGraph =
DirectedNavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DPassage>
