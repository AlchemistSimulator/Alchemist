package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.GraphEdge

/**
 * A heterogeneous pedestrian with cognitive and orienting capabilities.
 *
 * @param T the concentration type.
 * @param V the [Vector] type for the space this pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks stored in the pedestrian's [cognitiveMap].
 * @param E the type of edges of the [cognitiveMap].
 */
interface OrientingCognitivePedestrian<
    T,
    V : Vector<V>,
    A : GeometricTransformation<V>,
    N : ConvexGeometricShape<V, A>,
    E : GraphEdge<N>
> : OrientingPedestrian<T, V, A, N, E>, CognitivePedestrian<T>
