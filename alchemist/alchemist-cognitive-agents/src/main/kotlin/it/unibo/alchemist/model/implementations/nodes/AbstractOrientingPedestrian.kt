package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.actions.takePercentage
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.UndirectedNavigationGraph
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.pathExists
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph
import it.unibo.alchemist.shuffled
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.AsWeightedGraph
import org.jgrapht.graph.DefaultEdge

/**
 * An abstract [OrientingPedestrian].
 * This class defines an algorithm capable of generating a pseudo-random [cognitiveMap]
 * based on a [NavigationGraph] of the environment. The latter can be obtained from
 * [EnvironmentWithGraph]s, which is the only type of environment this pedestrian can
 * be placed into. The creation of landmarks is left to subclasses via factory method.
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space this pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks in the pedestrian's [cognitiveMap].
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 * @param F the type of the shape factory provided by the environment.
 *
 * The edges of the produced cognitive map are plain [DefaultEdge]s, which means no extra
 * information regarding the connections between landmarks is stored a part from the boolean
 * info concerning the fact that a connection exists. The path between two landmarks of the
 * cognitive map could or could not be simple (i.e. representable as a single line segment).
 */
abstract class AbstractOrientingPedestrian<T, P, A, N, M, E, F>(
    final override val knowledgeDegree: Double,
    /**
     * The random generator to use in order to preserve reproducibility.
     */
    protected val randomGenerator: RandomGenerator,
    /**
     * The environment this pedestrian is into.
     */
    environment: PhysicsEnvironmentWithGraph<*, T, P, A, M, E, F>,
    group: PedestrianGroup<T, P, A>? = null,
    /*
     * When generating the cognitive map, the regions whose diameter is
     * < of this quantity * the diameter of the agent will be discarded
     * and no landmark will be generated inside them.
     */
    private val minArea: Double = 10.0
) : OrientingPedestrian<T, P, A, N, DefaultEdge>,
    HomogeneousPedestrianImpl<T, P, A, F>(environment, randomGenerator, group)
    where
    P : Position<P>, P : Vector<P>,
    A : GeometricTransformation<P>,
    N : ConvexGeometricShape<P, A>,
    M : ConvexGeometricShape<P, A>,
    F : GeometricShapeFactory<P, A> {

    init {
        require(knowledgeDegree in 0.0..1.0) { "knowledge degree must be in [0,1]" }
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
    override val cognitiveMap: NavigationGraph<P, A, N, DefaultEdge> by lazy {
        val environmentGraph = environment.graph
        /*
         * The rooms in which landmarks will be placed.
         */
        val rooms = environmentGraph.vertexSet()
            .filter { it.diameter > shape.diameter * minArea }
            .shuffled(randomGenerator)
            .toList()
            .takePercentage(knowledgeDegree)
            .toMutableList()
        /*
         * landmarks[i] will contain the landmark generated in rooms[i].
         */
        val landmarks = rooms.map { generateLandmarkWithin(it) }
        val fullGraph = UndirectedNavigationGraph<P, A, N, DefaultEdge>(DefaultEdge::class.java)
        landmarks.forEach { fullGraph.addVertex(it) }
        rooms.indices.forEach { i ->
            rooms.indices.forEach { j ->
                if (i != j && environmentGraph.pathExists(rooms[i], rooms[j])) {
                    fullGraph.addEdge(landmarks[i], landmarks[j])
                }
            }
        }
        /*
         * The environment's graph is unweighted, but edges' weights defaults to 1.0
         */
        val dijkstra = DijkstraShortestPath(environmentGraph)
        val weightFunction: (DefaultEdge) -> Double = { edge ->
            val tail = fullGraph.getEdgeSource(edge)
            val head = fullGraph.getEdgeTarget(edge)
            /*
             * The weight of the shortest path between two rooms (tail, head) is the number
             * of rooms that need to be traversed to go from tail to head.
             */
            dijkstra.getPathWeight(rooms[landmarks.indexOf(tail)], rooms[landmarks.indexOf(head)])
        }
        val fullGraphWeighted = AsWeightedGraph(fullGraph, weightFunction, false, false)
        /*
         * Only the edges in the spanning tree are maintained.
         */
        fullGraph.removeAllEdges(
            fullGraph.edgeSet() - PrimMinimumSpanningTree(fullGraphWeighted).spanningTree.edges
        )
        fullGraph
    }

    /**
     * Generates a landmark entirely contained in the given region. If such region contains
     * one or more destinations, the generated landmark must contain at least one of them.
     */
    protected abstract fun generateLandmarkWithin(region: M): N
}
