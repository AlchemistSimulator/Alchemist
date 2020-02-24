package it.unibo.alchemist.model.implementations.reactions

import com.google.common.math.DoubleMath.fuzzyEquals
import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.geometry.graph.containsDestination
import it.unibo.alchemist.model.implementations.geometry.graph.destinationsWithin
import it.unibo.alchemist.model.implementations.geometry.graph.dijkstraShortestPath
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.graph.NavigationGraph
import it.unibo.alchemist.model.implementations.geometry.graph.nodeContaining
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData
import it.unibo.alchemist.model.interfaces.geometry.graph.OrientingAgent
import kotlin.math.pow

/**
 * A reaction representing the orienting behavior of a pedestrian. This class obtains
 * a route from the pedestrian's cognitive map and exploits it and the other
 * spatial information available to navigate the environment towards (or in search of)
 * a destination.
 * Two tasks are left to the derived classes via template method, see [computeEdgeRankings]
 * and [computeSubdestination].
 *
 * @param P  the [Position] type and [Vector] type for the space the pedestrian is inside.
 * @param A  the transformations supported by the shapes in this space.
 * @param N1 the type of nodes of the [envGraph].
 * @param E1 the type of edges of the [envGraph].
 * @param N2 the type of landmarks of the pedestrian's cognitive map.
 * @param T  the concentration type.
 *
 * The type of edges of the pedestrian's cognitive map is simply out GraphEdge<N2>, which means
 * this reaction doesn't exploit any information stored in those edges (basically, it assumes
 * no information is stored on them).
 */
abstract class AbstractOrientingBehavior<P, A : GeometricTransformation<P>, N1 : ConvexGeometricShape<P, A>, E1 : GraphEdge<N1>, N2: ConvexGeometricShape<P, A>, T>(
    private val env: Environment<T, P>,
    private val pedestrian: OrientingPedestrian<P, A, N2, out GraphEdge<N2>, T>,
    timeDistribution: TimeDistribution<T>,
    private val envGraph: NavigationGraph<P, A, N1, E1>
) : AbstractReaction<T>(pedestrian, timeDistribution) where P : Position<P>, P : Vector<P> {

    companion object {
        /*
         * Tolerance for Double comparisons.
         */
        private const val TOLERANCE = 1.0//1.0E-11
    }

    /*
     * Route to a possible destination derived from the cognitive map.
     */
    private val route: MutableList<N2> by lazy {
        with(pedestrian.cognitiveMap) {
            val p = env.getPosition(pedestrian)
            /*
             * Landmarks and destinations are sorted by the distance from the
             * pedestrian current position as the crow flies.
             */
            val closerLandmarks = nodes()
                .sortedBy { it.centroid.getDistanceTo(p) }
            val closerDestinations = destinations()
                .mapNotNull { nodeContaining(it) }
                .sortedBy { it.centroid.getDistanceTo(p) }
            /*
             * The pedestrian will look for a path leading from his closest
             * landmark to the closest destination possible.
             */
            var path: List<N2>? = null
            for (d in closerDestinations) {
                for (l in closerLandmarks) {
                    path = dijkstraShortestPath(l, d)?.path
                    if (path != null) {
                        break
                    }
                }
            }
            path?.toMutableList() ?: mutableListOf()
        }
    }
    /**
     * The room the pedestrian is into.
     */
    protected var currRoom: N1? = null
    /*
     * The room the pedestrian is heading to.
     */
    private var nextRoom: N1? = null
    /*
     * The position the pedestrian is moving towards, no obstacle is placed
     * between the agent and this position.
     */
    private var subdestination: P? = null
    /**
     * The edge (or better, crossing) the pedestrian is moving to.
     */
    protected var targetEdge: E1? = null
    /**
     */
    protected enum class State {
        /**
         */
        START,
        /**
         */
        NEW_ROOM,
        /**
         */
        MOVING_TO_DOOR,
        /**
         */
        CROSSING_DOOR,
        /**
         */
        MOVING_TO_FINAL,
        /**
         */
        ARRIVED
    }
    /**
     * This behavior is organised as a finite state machine
     */
    protected var state: State = State.START
    /**
     * Collection of areas which are found to be impasses (and thus
     * will be avoided).
     */
    private val impasses: MutableSet<N1> = mutableSetOf()

    /**
     */
    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) {}

    /**
     */
    override fun getRate(): Double = timeDistribution.rate

    /**
     */
    override fun execute() {
        var p = env.getPosition(pedestrian)
        when (state) {
            State.START -> {
                currRoom = envGraph.nodeContaining(p)
                if (currRoom != null) {
                    state = State.NEW_ROOM
                }
                /*
                 * If the pedestrian cannot locate itself inside any room (unusual
                 * condition), it tries to reach the closest door/passage in order
                 * to enter one. If this isn't possible, it simply won't move.
                 */
                else {
                    val closestDoor = envGraph.nodes()
                        .flatMap { envGraph.edgesFrom(it) }
                        .map { it to computeSubdestination(it) }
                        .minBy { it.second.getDistanceTo(p) }
                    if (closestDoor != null) {
                        nextRoom = closestDoor.first.to
                        subdestination = closestDoor.second
                        targetEdge = closestDoor.first
                        state = State.MOVING_TO_DOOR
                    } else {
                        state = State.ARRIVED
                    }
                }
            }
            State.NEW_ROOM -> {
                if (nextRoom != null) {
                    currRoom = if (nextRoom!!.contains(p)) {
                        nextRoom!!
                    } else {
                        envGraph.nodes().first { it.contains(p) }
                    }
                }
                pedestrian.registerVisit(currRoom!!)
                if (envGraph.edgesFrom(currRoom!!).size == 1) {
                    impasses.add(currRoom!!)
                }
                with (envGraph.destinationsWithin(currRoom!!)) {
                    if (isNotEmpty()) {
                        route.clear()
                        subdestination = first()
                        state = State.MOVING_TO_FINAL
                        return
                    }
                }
                /*
                 * If the next sub-destination of the route is in sight, remove it.
                 */
                if (route.isNotEmpty() && route.any { currRoom!!.contains(it.centroid) }) {
                    for (i in 0..route.indexOfFirst { currRoom!!.contains(it.centroid) }) {
                        route.removeAt(i)
                    }
                }
                val rankings = if (route.isNotEmpty()) {
                    computeEdgeRankings(currRoom!!, route[0].centroid)
                } else {
                    null
                }
                val edge = envGraph.edgesFrom(currRoom!!)
                    .map { it to weight(it, rankings?.get(it)) }
                    .minWith(
                        compareBy({
                            it.second
                        }, {
                            /*
                             * nearest door heuristic
                             */
                            computeSubdestination(it.first).getDistanceTo(p)
                        })
                    )?.first
                if (edge != null) {
                    nextRoom = edge.to
                    subdestination = computeSubdestination(edge)
                    targetEdge = edge
                    state = State.MOVING_TO_DOOR
                }
                /*
                 * Closed room, we can't move anywhere.
                 */
                else {
                    state = State.ARRIVED
                }
            }
            State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> {
                moveTowards(currRoom!!, subdestination!!, p)
                p = env.getPosition(pedestrian)
                when (state) {
                    State.CROSSING_DOOR -> {
                        if (nextRoom!!.contains(p) || envGraph.nodes().any { it != currRoom && it.contains(p) }) {
                            state = State.NEW_ROOM
                        }
                    }
                    else -> {
                        val arrived = fuzzyEquals(p.getDistanceTo(subdestination!!), 0.0, TOLERANCE)
                        if (state == State.MOVING_TO_DOOR) {
                            if (arrived) {
                                subdestination = nextRoom!!.centroid
                                state = State.CROSSING_DOOR
                            } else {
                                /*
                                 * Recomputes sub-destination
                                 */
                                subdestination = computeSubdestination(targetEdge!!)
                            }
                        } else if (arrived) {
                            state = State.ARRIVED
                        }
                    }
                }
            }
            State.ARRIVED -> {}
        }
    }

    /**
     * Maps each edge outgoing from the given room to an integer rank indicating its
     * suitability in order to reach the provided destination. Rankings should be in [1,N].
     * A lower rank means the correspondent edge is preferable to reach the given destination.
     *
     * When following a route provided by the cognitive map, the agent often is in the following
     * situation: he knows the spatial structure of the current room and the next sub-destination
     * he want to reach, but has no other information regarding the spatial structure between
     * his room and that position (remember that it is assumed the cognitive map provide only
     * a boolean information regarding the connection of landmarks, no extra info is stored).
     * This method should implement an algorithm allowing the pedestrian to perform an educated
     * guess of which crossing to take in order to get closer to the provided destination.
     */
    protected abstract fun computeEdgeRankings(currRoom: N1, destination: P): Map<E1, Int>

    /**
     * Computes the next sub-destination the pedestrian will move towards, provided an edge the
     * pedestrian wants to cross. The provided edge belongs to the room the agent is located into
     * (i.e. it is in sight of the agent), the returned position must be in sight of him as well,
     * which means no obstacle should be placed between such destination and his current
     * position.
     *
     * This method is mainly about exploiting the extra data stored in the edge (for instance
     * its shape and location in the room boundary) to determine which point the pedestrian
     * shall point towards.
     */
    protected abstract fun computeSubdestination(targetEdge: E1): P

    /**
     * Move the pedestrian towards a position which is guaranteed to be in sight (i.e. no
     * obstacle is placed between him and such position).
     */
    protected open fun moveTowards(currRoom: N1, target: P, currPosition: P) {
        Seek(env, this, pedestrian, *target.cartesianCoordinates).execute()
    }

    /**
     * Weights an edge.
     */
    protected open fun weight(e: E1, rank: Int?): Double {
        val fVolatileMem = 2.0.pow(pedestrian.volatileMemory[e.to] ?: 0)
        var fCognitiveMap = 1.0
        rank?.let { fCognitiveMap -= 0.5.pow(rank) }
        val fFinal = if (envGraph.containsDestination(e.to)) 0.1 else 1.0
        //val fImpasse = if (impasses.contains(e.to)) 10.0 else 1.0
        val fImpasse = if (isImpasse(e.to)) 10.0 else 1.0
        return fVolatileMem * fCognitiveMap * fFinal * fImpasse
    }

    /*
     * Registers visit in volatile memory.
     */
    private fun OrientingAgent<P, A, N2, *>.registerVisit(to: N1) {
        volatileMemory[to] = (volatileMemory[to] ?: 0) + 1
    }

    private fun isImpasse(area: N1): Boolean =
        pedestrian.volatileMemory.contains(area) &&
            envGraph.edgesFrom(area).map { it.to }.distinct().count() <= 1
}
