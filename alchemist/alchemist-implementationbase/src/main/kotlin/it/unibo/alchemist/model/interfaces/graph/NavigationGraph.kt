package it.unibo.alchemist.model.interfaces.graph

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D

/**
 * A graph used for navigation purposes. Nodes are [ConvexGeometricShape]s,
 * usually representing portions of an environment which are traversable by
 * agents (the advantage of such representation is that agents can freely
 * walk around within a convex area, as it is guaranteed that no obstacle
 * will be found).
 * Additionally, a navigation graph can store a set of positions of interest
 * that may be used during navigation (e.g. destinations in an evacuation
 * scenario).
 * Note that implementations of this graph should guarantee predictable
 * ordering for the collections they maintain, as reproducibility is a key
 * feature of Alchemist.
 *
 * @param V the [Vector] type for the space this graph describes.
 * @param A the transformations supported by shapes in this space.
 * @param N the type of nodes (or vertices).
 * @param E the type of edges.
 */
interface NavigationGraph<
    V : Vector<V>,
    A : GeometricTransformation<V>,
    N : ConvexGeometricShape<V, A>,
    E
> : org.jgrapht.Graph<N, E> {

    /**
     * A list of positions of interest (usually destinations).
     */
    fun destinations(): List<V>

    /**
     * @returns the destinations within the provided [node].
     */
    fun destinationsWithin(node: N): Collection<V> = destinations().filter { node.contains(it) }

    /**
     * Checks whether the provided [node] contains any destination.
     */
    fun containsAnyDestination(node: N): Boolean = destinations().any { node.contains(it) }

    /**
     * @returns the first node containing the specified [destination],
     * null if no node containing it could be found.
     */
    fun nodeContaining(destination: V): N? = vertexSet().firstOrNull { it.contains(destination) }
}

/**
 * A crossing in an euclidean bidimensional space, represented as a [Segment2D].
 * The segment models the shape of the passage between two areas: an agent could cross
 * any point of it to move from the first area to the second one. To make it easier, think
 * of the following: in an indoor environment, the segment should represent the shape of
 * the door between two rooms. Additionally, given a crossing c connecting area a to area
 * b, the segment MUST belong to the boundary of a, but can or cannot belong the boundary
 * of b.
 */
typealias Euclidean2DCrossing = Segment2D<Euclidean2DPosition>

/**
 * A [NavigationGraph] in an euclidean bidimensional space, whose nodes
 * are [ConvexPolygon]s and edges are [Euclidean2DCrossing]s.
 */
typealias Euclidean2DNavigationGraph =
    NavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DCrossing>
