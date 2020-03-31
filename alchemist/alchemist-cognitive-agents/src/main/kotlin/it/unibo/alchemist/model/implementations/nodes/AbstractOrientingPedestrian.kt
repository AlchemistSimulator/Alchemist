package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.graph.builder.NavigationGraphBuilder
import it.unibo.alchemist.model.implementations.graph.builder.addEdge
import it.unibo.alchemist.model.implementations.geometry.liesBetween
import it.unibo.alchemist.model.implementations.graph.containsAnyDestination
import it.unibo.alchemist.model.implementations.graph.destinationsWithin
import it.unibo.alchemist.model.implementations.graph.isReachable
import it.unibo.alchemist.model.implementations.graph.primMinimumSpanningForest
import it.unibo.alchemist.model.implementations.graph.dijkstraShortestPath
import it.unibo.alchemist.model.implementations.utils.shuffled
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import org.apache.commons.math3.random.RandomGenerator

/**
 * An abstract [OrientingPedestrian].
 * This class defines an algorithm capable of generating a pseudo-random [cognitiveMap]
 * based on a [NavigationGraph] of the environment. The latter can be obtained from
 * [Environment2DWithGraph]s, which is the only type of environment this pedestrian can
 * be placed into. The creation of landmarks is left to subclasses via factory method.
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space this pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks in the pedestrian's [cognitiveMap].
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param F the type of edges of the navigation graph provided by the environment.
 *
 * The edges of the produced cognitive map are plain [GraphEdge]s, which means no extra
 * information regarding the connections between landmarks is stored a part from the boolean
 * info concerning the fact that a connection exists. The path between two landmarks of the
 * cognitive map could or could not be simple (i.e. representable as a single line segment).
 */
abstract class AbstractOrientingPedestrian<
    T,
    P,
    A : GeometricTransformation<P>,
    N : ConvexGeometricShape<P, A>,
    M : ConvexGeometricShape<P, A>,
    F : GraphEdge<M>
>(
    final override val knowledgeDegree: Double,
    /**
     * The random generator to use in order to preserve reproducibility.
     */
    protected val randomGenerator: RandomGenerator,
    /**
     * The environment this pedestrian is into.
     */
    protected open val environment: Environment2DWithGraph<*, T, P, A, M, F>,
    group: PedestrianGroup<T>? = null,
    /*
     * When generating the cognitive map, the regions whose diameter is
     * < of this quantity * the diameter of the agent will be discarded
     * and no landmark will be generated inside them.
     */
    private val minArea: Double = 10.0
) : OrientingPedestrian<T, P, A, N, GraphEdge<N>>,
    HomogeneousPedestrianImpl<T, P>(environment, randomGenerator, group) where P : Position<P>, P : Vector<P> {

    init {
        require(knowledgeDegree.liesBetween(0.0, 1.0)) { "knowledge degree must be in [0,1]" }
    }

    override val volatileMemory: MutableMap<in ConvexGeometricShape<P, A>, Int> = HashMap()

    /*
     * Here's a brief description of how the algorithm operates:
     * We randomly select a % of environment's regions equal to the knowledge 
     * degree of the pedestrian, we then generate a landmark in each of them
     * (via factory method). Concerning the connections between landmarks, we
     * produce a graph in which each landmark is connected to any other landmark
     * reachable from it, with an edge whose weight depends on the number of
     * rooms that need to be traversed (information on reachability between regions
     * and number of areas to be traversed can be obtained from the environment's
     * graph). We then produce a minimum spanning tree of the described graph.
     */
    override val cognitiveMap: NavigationGraph<P, A, N, GraphEdge<N>> by lazy {
        val builder = NavigationGraphBuilder<P, A, N, GraphEdge<N>>()
        val environmentGraph: NavigationGraph<P, A, M, F> = environment.graph()
        /*
         * The rooms in which landmarks will be placed.
         */
        val rooms = environmentGraph.nodes()
            .filter { it.diameter > shape.diameter * minArea || environmentGraph.containsAnyDestination(it) }
            .shuffled(randomGenerator)
            .toList()
            .takePercentage(knowledgeDegree)
            .toMutableList()
        /*
         * At least one destination is provided if knowledge degree >= 0.1.
         */
        if (rooms.none { environmentGraph.containsAnyDestination(it) } && knowledgeDegree >= minimumKnowledgeDegree) {
            environmentGraph.nodes()
                .firstOrNull { environmentGraph.containsAnyDestination(it) }
                ?.let { rooms.add(it) }
        }
        val landmarks = rooms.map { generateLandmarkWithin(it) }
        /*
         * Maps each landmark's index to the indices of the ones reachable from it.
         */
        val reachability = rooms.indices
            .map { i ->
                i to rooms.indices.filter { j -> i != j && environmentGraph.isReachable(rooms[i], rooms[j]) }
            }.toMap()
        reachability.forEach {
            it.value.forEach { i -> builder.addEdge(landmarks[it.key], landmarks[i]) }
        }
        builder.build(rooms.flatMap { environmentGraph.destinationsWithin(it) })
            .primMinimumSpanningForest { edge ->
                environmentGraph.dijkstraShortestPath(
                    rooms[landmarks.indexOf(edge.tail)],
                    rooms[landmarks.indexOf(edge.head)],
                    { 1.0 }
                )?.weight ?: edge.tail.centroid.getDistanceTo(edge.head.centroid)
            }
    }

    /**
     * Generates a landmark entirely contained in the given region. If such region contains
     * one or more destinations, the generated landmark must contain at least one of them.
     */
    protected abstract fun generateLandmarkWithin(region: M): N

    private fun <E> List<E>.takePercentage(percentage: Double) = subList(0, (percentage * size).toInt())

    companion object {
        private const val minimumKnowledgeDegree = 0.1
    }
}
