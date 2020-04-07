package it.unibo.alchemist.model.implementations.graph

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DNavigationGraph
import org.jgrapht.graph.DefaultDirectedGraph

/**
 * An implementation of [NavigationGraph], deriving from [DefaultDirectedGraph].
 * Note that vertices and edges are used as keys inside [DefaultDirectedGraph],
 * so when choosing their types, you must follow these rules:
 * - You must follow the contract defined in java.lang.Object for both equals and hashCode.
 * - In particular, if you override either equals or hashCode, you must override them both.
 * - Your implementation for hashCode must produce a value which does not change over the
 * lifetime of the object.
 * Further information available [here](https://jgrapht.org/guide/VertexAndEdgeTypes).
 */
class DefaultNavigationGraph<
    V : Vector<V>,
    A : GeometricTransformation<V>,
    N : ConvexGeometricShape<V, A>,
    E
>(
    private val destinations: List<V>,
    edgeClass: Class<out E>
) : NavigationGraph<V, A, N, E>,
    /*
     * Guarantees predictable iteration order, see https://jgrapht.org/guide/UserOverview para. 4.
     */
    DefaultDirectedGraph<N, E>(edgeClass) {

    override fun destinations(): List<V> = destinations
}

/**
 * Default implementation of [Euclidean2DNavigationGraph].
 */
typealias DefaultEuclidean2DNavigationGraph =
    DefaultNavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DPassage>
