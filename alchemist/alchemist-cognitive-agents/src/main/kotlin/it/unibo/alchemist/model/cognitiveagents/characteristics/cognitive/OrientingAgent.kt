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
     * value in [0, 1] describing the percentage of the environment the pedestrian
     * is familiar with prior to the start of the simulation (thus it does not take
     * into account the knowledge the pedestrian will gain during it, namely the
     * [volatileMemory]).
     */
    val knowledgeDegree: Double

    /**
     * The cognitive map of the agent. A cognitive map is composed of landmarks
     * (elements of the environment easy to remember due to their uniqueness)
     * and spatial relations between them, it is represented using a [NavigationGraph].
     */
    val cognitiveMap: NavigationGraph<V, A, N, E>

    /**
     * The volatile memory of the agent, it models the ability to remember rooms
     * or spaces of the environment already visited by the agent since the start
     * of the simulation. In particular, each room is paired with the number of
     * visits.
     */
    val volatileMemory: MutableMap<in ConvexGeometricShape<V, A>, Int>

    /**
     * Registers a visit to the provided [area] in the pedestrian's volatile memory.
     */
    fun <M : ConvexGeometricShape<V, A>> registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }
}
