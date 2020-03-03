package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.OrientingAgent
import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.graph.containsDestination
import it.unibo.alchemist.model.implementations.graph.destinationsWithin
import it.unibo.alchemist.model.implementations.graph.dijkstraShortestPath
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.NavigationGraph
import it.unibo.alchemist.model.implementations.graph.nodeContaining
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import kotlin.math.pow

/**
 * A reaction representing the orienting behavior of a pedestrian. This class obtains
 * a route from the pedestrian's cognitive map and exploits it and the other
 * spatial information available to navigate the environment towards (or in search of)
 * a destination.
 * Two tasks are left to the derived classes via template method, see [computeEdgeRankings]
 * and [computeSubdestination].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of nodes of the [environmentGraph].
 * @param E the type of edges of the [environmentGraph].
 * @param M the type of landmarks of the pedestrian's cognitive map.
 * @param F the type of edges of the pedestrian's cognitive map.
 *
 * Since F is simply any subtype of GraphEdge<M>, this reaction assumes no information
 * is stored in the edges of the cognitive map.
 */
abstract class AbstractOrientingBehavior<
    T,
    P,
    A : GeometricTransformation<P>,
    N : ConvexGeometricShape<P, A>,
    E : GraphEdge<N>,
    M : ConvexGeometricShape<P, A>,
    F : GraphEdge<M>
>(
    /**
     * The environment the pedestrian is into.
     */
    protected val environment: Environment<T, P>,
    /**
     * The owner of this behavior.
     */
    protected val pedestrian: OrientingPedestrian<T, P, A, M, F>,
    timeDistribution: TimeDistribution<T>,
    /**
     * A navigation graph describing the environment. Nodes are [ConvexGeometricShape]s
     * that should represent the walkable areas of the environment (i.e. the areas that
     * are freely traversable by agents). Edges represent connections between these
     * areas. Additionally, a [NavigationGraph] can store some destinations which
     * will be considered as possible final destinations.
     */
    protected val environmentGraph: NavigationGraph<P, A, N, E>
) : AbstractReaction<T>(pedestrian, timeDistribution) where P : Position<P>, P : Vector<P> {

    /*
     * When navigating towards a sub-destination, such target will be considered
     * reached when the pedestrian's distance from it is <= of this quantity.
     *
     * Considering a target reached when the distance from it it's (fuzzy) equal to
     * zero may still lead to some extreme cases in which pedestrians remain blocked
     * due to how the environment manage collisions (namely, if a pedestrian wants
     * to reach an already occupied position, it can't move at all, it can't even
     * approach such position). This workaround allows to specify a minDistance
     * which is not absolute, instead it's dependent on the pedestrian shape. In the
     * future, something better could be done.
     */
    private val minDistance = pedestrian.shape.diameter

    /*
     * Route to a possible destination derived from the cognitive map.
     */
    private val route: MutableList<M> by lazy {
        with(pedestrian.cognitiveMap) {
            val currPos = environment.getPosition(pedestrian)
            /*
             * Landmarks and destinations are sorted by the distance from the
             * pedestrian current position as the crow flies.
             */
            val closerLandmarks = nodes()
                .sortedBy { it.centroid.getDistanceTo(currPos) }
            val closerDestinations = destinations()
                .mapNotNull { nodeContaining(it) }
                .sortedBy { it.centroid.getDistanceTo(currPos) }
            /*
             * The pedestrian will look for a path leading from his closest
             * landmark to the closest destination possible.
             */
            closerDestinations.map { d ->
                closerLandmarks.mapNotNull {
                    /*
                     * At present the cognitive map is a MST, so there's a single
                     * path between each pair of nodes. In the future, things may
                     * change and there could be more than one shortest path between
                     * two nodes. In this case, it may be preferable to choose a
                     * shortest path with the maximum number of edges possible. The
                     * reason is that such a path contains more detailed information
                     * regarding the route to follow.
                     */
                    l -> dijkstraShortestPath(l, d)?.path
                }.first()
            }.firstOrNull()?.toMutableList() ?: mutableListOf()
        }
    }
    /*
     * The room (or walkable area) the pedestrian is into.
     */
    private lateinit var currRoom: N
    /*
     * The room (or walkable area) the pedestrian is heading to.
     */
    private lateinit var nextRoom: N
    /*
     * The position the pedestrian is moving towards, no obstacle is placed
     * between the agent and this position.
     */
    private lateinit var subdestination: P
    /*
     * The edge (or better, crossing) the pedestrian is moving towards.
     */
    private lateinit var targetEdge: E
    /*
     */
    private enum class State {
        START,
        NEW_ROOM,
        /*
         * There is one unusual case in which the pedestrian cannot locate itself
         * inside any room of the [environmentGraph], and decides to try to reach
         * the closest edge possible. In such case, [currRoom] won't be initialised
         * yet.
         */
        MOVING_TO_DOOR,
        /*
         * Adjacent rooms are not guaranteed to be exactly geometrically adjacent,
         * there can be some distance between them. Consequently, the pedestrian could
         * find itself in a situation in which he/she is crossing a door but cannot locate
         * itself in any room. In this case we just proceed towards the center of the room
         * we want to reach.
         */
        CROSSING_DOOR,
        MOVING_TO_FINAL,
        ARRIVED
    }
    /*
     * This behavior is organised as a finite state machine
     */
    private var state: State = State.START

    /**
     */
    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) {}

    /**
     */
    override fun getRate(): Double = timeDistribution.rate

    /**
     */
    override fun execute() {
        var currPos = environment.getPosition(pedestrian)
        when (state) {
            State.START -> {
                with(environmentGraph.nodeContaining(currPos)) {
                    if (this != null) {
                        state = State.NEW_ROOM
                    }
                    /*
                     * If the pedestrian cannot locate itself inside any room (unusual
                     * condition), it tries to reach the closest door/passage in order
                     * to enter one. If this isn't possible, it simply won't move.
                     */
                    else {
                        val closestDoor = environmentGraph.nodes()
                            .flatMap { environmentGraph.edgesFrom(it) }
                            .map { it to computeSubdestination(it) }
                            .minBy { it.second.getDistanceTo(currPos) }
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
            }
            State.NEW_ROOM -> {
                currRoom = if (::nextRoom.isInitialized && nextRoom.contains(currPos)) {
                    nextRoom
                } else {
                    environmentGraph.nodes().first { it.contains(currPos) }
                }
                pedestrian.registerVisit(currRoom)
                with(environmentGraph.destinationsWithin(currRoom)) {
                    if (isNotEmpty()) {
                        route.clear()
                        subdestination = first()
                        state = State.MOVING_TO_FINAL
                        return
                    }
                }
                /*
                 * If a sub-destination of the route is in sight, updates the route removing
                 * all the sub-destination up to the one encountered.
                 */
                if (route.isNotEmpty() && route.any { currRoom.contains(it.centroid) }) {
                    for (i in 0..route.indexOfFirst { currRoom.contains(it.centroid) }) {
                        route.removeAt(0)
                    }
                }
                val rankings = if (route.isNotEmpty()) {
                    computeEdgeRankings(currRoom, route[0].centroid)
                } else {
                    null
                }
                /*
                 * The pedestrian can see and assess all the edges outgoing from the current room
                 */
                val edge = environmentGraph.edgesFrom(currRoom)
                    .minWith(
                        compareBy({
                            weight(it, rankings?.get(it))
                        }, {
                            /*
                             * nearest door heuristic
                             */
                            computeSubdestination(it).getDistanceTo(currPos)
                        })
                    )
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
                moveTowards(subdestination, if (::currRoom.isInitialized) currRoom else null, targetEdge)
                currPos = environment.getPosition(pedestrian)
                if (environmentGraph.nodes().any { (!::currRoom.isInitialized || it != currRoom) && it.contains(currPos) }) {
                    state = State.NEW_ROOM
                } else if (state != State.CROSSING_DOOR) {
                    val arrived = currPos.getDistanceTo(subdestination) <= minDistance
                    if (state == State.MOVING_TO_DOOR) {
                        if (arrived) {
                            /*
                             * This may cause some unrealistic deviations, a possible alternative
                             * is to move the pedestrian in the same direction it moved so far.
                             */
                            subdestination = nextRoom.centroid
                            state = State.CROSSING_DOOR
                        } else {
                            /*
                             * Recomputes sub-destination
                             */
                            subdestination = computeSubdestination(targetEdge)
                        }
                    } else if (arrived) {
                        state = State.ARRIVED
                    }
                }
            }
            State.ARRIVED -> {}
        }
    }

    /**
     * Maps each edge outgoing from the given room to an integer rank indicating its
     * suitability in order to reach the provided destination (which is outside of the scope
     * of the agent). Rankings should be in [1,N], a lower rank means the correspondent edge
     * is preferable to reach the given destination.
     *
     * When following a route provided by the cognitive map, the agent often is in the following
     * situation: he knows the spatial structure of the current room and the next sub-destination
     * he want to reach, but has no other information regarding the spatial structure between
     * his room and that position (remember that it is assumed the cognitive map provide only
     * a boolean information regarding the connection of landmarks, no extra info is stored).
     * This method should implement an algorithm allowing the pedestrian to perform an educated
     * guess of which crossing to take in order to get closer to the provided destination.
     */
    protected abstract fun computeEdgeRankings(currentRoom: N, destination: P): Map<E, Int>

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
    protected abstract fun computeSubdestination(targetEdge: E): P

    /**
     * Move the pedestrian towards a position which is guaranteed to be in sight (i.e. no
     * obstacle is placed between him and such position).
     */
    protected open fun moveTowards(target: P, currentRoom: N?, targetEdge: E): Unit =
        Seek(environment, this, pedestrian, *target.cartesianCoordinates).execute()

    /**
     * Assign a weight to a given edge. The one with minimum weight will be chosen and crossed.
     * @param rank is the rank given to the edge when assessing its suitability to reach the
     * next subdestination. See [cognitiveMapFactor].
     */
    protected open fun weight(edge: E, rank: Int?): Double =
        volatileMemoryFactor(edge) * cognitiveMapFactor(rank) * finalDestinationFactor(edge) * impasseFactor(edge)

    /*
     * Computes the factor deriving from the pedestrian's volatile memory for the
     * weighting system. It is computed as 2^k where k is the number of visits
     * to the area the edge being weighted leads to.
     */
    private fun volatileMemoryFactor(edge: E) = 2.0.pow(pedestrian.volatileMemory[edge.to] ?: 0)

    /*
     * Computes the factor deriving from the pedestrian's cognitive map for the
     * weighting system. It is computed as 1 - 0.5^i where i is the rank given
     * to the edge assessing its suitability to reach the next subdestination.
     * If rank is null, the factor is 1.
     */
    private fun cognitiveMapFactor(rank: Int?) = 1.0 - (rank?.let { 0.5.pow(it) } ?: 0.0)

    /*
     * Computes the factor for the weighting system taking into account final
     * destinations discovered while travelling.
     */
    private fun finalDestinationFactor(edge: E) = if (environmentGraph.containsDestination(edge.to)) 0.1 else 1.0

    /*
     * Computes the factor for the weighting system taking into account whereas
     * the assessed edge leads to an impasse or not.
     */
    private fun impasseFactor(edge: E) = if (isImpasse(edge.to)) 10.0 else 1.0

    /*
     * Registers a visit in the given area in the pedestrian's volatile memory.
     */
    private fun OrientingAgent<P, A, M, *>.registerVisit(area: N) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }

    /*
     * Checks if the pedestrian knows that the given area is an impasse
     * (i.e. with a single door).
     */
    private fun isImpasse(area: N): Boolean =
        pedestrian.volatileMemory.contains(area) &&
            environmentGraph.edgesFrom(area).map { it.to }.distinct().count() <= 1
}
