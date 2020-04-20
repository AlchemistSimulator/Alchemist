package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.OrientingAgent
import it.unibo.alchemist.model.implementations.actions.Seek
import it.unibo.alchemist.model.implementations.graph.containsAnyDestination
import it.unibo.alchemist.model.implementations.graph.destinationsWithin
import it.unibo.alchemist.model.implementations.graph.dijkstraShortestPath
import it.unibo.alchemist.model.implementations.graph.nodeContaining
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Environment2DWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import kotlin.math.pow

/**
 * A reaction representing the orienting behavior of an [OrientingPedestrian].
 * This class obtains a route from the pedestrian's cognitive map and exploits that and the
 * other spatial information available to navigate the environment towards (or in search of)
 * a destination.
 * Two tasks are left to the derived classes via template method, see [computeEdgeRankings]
 * and [computeSubdestination].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is inside.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the [environment].
 * @param F the type of edges of the navigation graph provided by the [environment].
 *
 * Since E is simply any subtype of GraphEdge<N>, this reaction assumes no extra information
 * is stored in the edges of the cognitive map.
 */
abstract class AbstractOrientingBehavior<T, A, N, E, M, F>(
    /**
     * The environment the pedestrian is into.
     */
    protected open val environment: Environment2DWithGraph<*, T, Euclidean2DPosition, A, M, F>,
    /**
     * The owner of this behavior.
     */
    protected val pedestrian: OrientingPedestrian<T, Euclidean2DPosition, A, N, E>,
    timeDistribution: TimeDistribution<T>
) : AbstractReaction<T>(pedestrian, timeDistribution)
    where
        A : GeometricTransformation<Euclidean2DPosition>,
        N : ConvexGeometricShape<Euclidean2DPosition, A>,
        E : GraphEdge<N>,
        M : ConvexGeometricShape<Euclidean2DPosition, A>,
        F : GraphEdge<M> {

    /*
     * When navigating towards a sub-destination, such target will be considered
     * reached when the pedestrian's distance from it is <= of this quantity.
     *
     * Considering a target reached when the distance from it it's (fuzzy) equal to
     * zero may still lead to some extreme cases in which pedestrians remain blocked
     * due to how the environment manage collisions (namely, if a pedestrian wants
     * to reach a position already occupied by someone, it can't move at all, it can't
     * even approach such position). This workaround allows to specify a minDistance
     * which is not absolute, instead it's dependent on the pedestrian shape. In the
     * future, something better could be done.
     */
    private val minDistance = pedestrian.shape.diameter

    /*
     * Route to a possible destination obtained from the cognitive map.
     */
    private val route: MutableList<N> by lazy {
        with(pedestrian.cognitiveMap) {
            val currPos = environment.getPosition(pedestrian)
            /*
             * Landmarks and destinations are sorted by the distance from the
             * pedestrian current position as the crow flies.
             */
            val closerLandmarks = nodes()
                .sortedBy { it.centroid.distanceTo(currPos) }
            val closerDestinations = destinations()
                .mapNotNull { nodeContaining(it) }
                .sortedBy { it.centroid.distanceTo(currPos) }
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
    private lateinit var currRoom: M
    /*
     * The room (or walkable area) the pedestrian is heading to.
     */
    private lateinit var nextRoom: M
    /*
     * The position the pedestrian is moving towards, no obstacle is placed
     * between the agent and this position.
     */
    private lateinit var subdestination: Euclidean2DPosition
    /*
     * The edge the pedestrian is moving towards.
     */
    private lateinit var targetDoor: F

    private enum class State {
        START,
        NEW_ROOM,
        /*
         * There is one unusual case in which the pedestrian cannot locate itself
         * inside any room of the environment's graph, and decides to try to reach
         * the closest crossing possible. In such case, the state will be MOVING_TO
         * _DOOR, but the [currRoom] variable won't be initialised yet.
         */
        MOVING_TO_DOOR,
        /*
         * Adjacent rooms are not guaranteed to be exactly geometrically adjacent,
         * there can be some distance between them. Consequently, the pedestrian could
         * find itself in a situation in which it is crossing a door but cannot locate
         * itself inside any room. Such case is identified by CROSSING_DOOR: in this case,
         * the pedestrian just proceeds towards the center of the room it wants to reach.
         */
        CROSSING_DOOR,
        MOVING_TO_FINAL,
        ARRIVED
    }
    private var state: State = State.START

    override fun updateInternalStatus(curTime: Time?, executed: Boolean, env: Environment<T, *>?) = Unit

    override fun getRate(): Double = timeDistribution.rate

    private fun onStart() {
        val currentPosition = environment.myPosition
        with(environment.graph().nodeContaining(currentPosition)) {
            if (this != null) {
                state = State.NEW_ROOM
            }
            /*
             * If the pedestrian cannot locate itself inside any room (unusual
             * condition), it tries to reach the closest door/passage in order
             * to enter one. If this isn't possible, it simply won't move.
             */
            else {
                val closestDoor = environment.graph().nodes()
                    .flatMap { environment.graph().edgesFrom(it) }
                    .map { it to computeSubdestination(it) }
                    .minBy { it.second.distanceTo(currentPosition) }
                if (closestDoor != null) {
                    nextRoom = closestDoor.first.head
                    subdestination = closestDoor.second
                    targetDoor = closestDoor.first
                    state = State.MOVING_TO_DOOR
                } else {
                    state = State.ARRIVED
                }
            }
        }
    }

    private fun inNewRoom() {
        val currentPosition = environment.myPosition
        currRoom = if (::nextRoom.isInitialized && nextRoom.contains(currentPosition)) {
            nextRoom
        } else {
            environment.graph().nodes().first { it.contains(currentPosition) }
        }
        pedestrian.registerVisit(currRoom)
        with(environment.graph().destinationsWithin(currRoom)) {
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
            for (i in 0..route.indexOfLast { currRoom.contains(it.centroid) }) {
                route.removeAt(0)
            }
        }
        val rankings = if (route.isNotEmpty()) {
            computeEdgeRankings(currRoom, route[0].centroid)
        } else {
            null
        }
        /*
         * The pedestrian can see all the edges outgoing from the current room.
         */
        val targetEdge = environment.graph().edgesFrom(currRoom)
            .minWith(
                compareBy({
                    weight(it, rankings?.get(it))
                }, {
                    /*
                     * nearest door heuristic
                     */
                    computeSubdestination(it).distanceTo(currentPosition)
                })
            )
        if (targetEdge != null) {
            nextRoom = targetEdge.head
            subdestination = computeSubdestination(targetEdge)
            targetDoor = targetEdge
            state = State.MOVING_TO_DOOR
        }
        /*
         * Closed room, we can't move anywhere.
         */
        else {
            state = State.ARRIVED
        }
    }

    private fun moving() {
        moveTowards(subdestination, if (::currRoom.isInitialized) currRoom else null, targetDoor)
        val currentPosition = environment.myPosition
        val inNewRoom = environment.graph().nodes()
            .any {
                (!::currRoom.isInitialized || it != currRoom) && it.contains(currentPosition)
            }
        if (inNewRoom) {
            state = State.NEW_ROOM
        } else if (state != State.CROSSING_DOOR) {
            val arrived = currentPosition.distanceTo(subdestination) <= minDistance
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
                    subdestination = computeSubdestination(targetDoor)
                }
            } else if (arrived) {
                state = State.ARRIVED
            }
        }
    }

    private val Environment<T, Euclidean2DPosition>.myPosition: Euclidean2DPosition get() = getPosition(pedestrian)

    override fun execute() = when (state) {
        State.START -> onStart()
        State.NEW_ROOM -> inNewRoom()
        State.MOVING_TO_DOOR, State.CROSSING_DOOR, State.MOVING_TO_FINAL -> moving()
        State.ARRIVED -> {}
    }

    /**
     * Maps each edge outgoing from the given room to an integer rank indicating its
     * suitability in order to reach the provided destination (which is outside of the scope
     * of the agent). Rankings should be in [1,E], where E is the number of edges, a lower
     * rank means the correspondent edge is preferable to reach the given destination.
     *
     * When following a route provided by the cognitive map, the agent often is in the following
     * situation: he knows the spatial structure of the current room and the next sub-destination
     * he want to reach, but has no other information regarding the spatial structure between
     * his room and that position (remember that it is assumed the cognitive map provide only
     * a boolean information regarding the connection of landmarks, no extra info is stored).
     * This method should implement an algorithm allowing the pedestrian to perform an educated
     * guess of which crossing to take in order to get closer to the provided destination.
     */
    protected abstract fun computeEdgeRankings(currentRoom: M, destination: Euclidean2DPosition): Map<F, Int>

    /**
     * Computes the next sub-destination the pedestrian will move towards, provided an edge he
     * wants to cross. The provided edge belongs to the room the agent is into (i.e. it is in
     * sight of the agent), the returned position must be in sight of him as well, which means
     * no obstacle should be placed between such destination and his current position.
     *
     * This method is mainly about exploiting the extra data stored in the edge (for instance
     * its shape and location in the room boundary) to determine which point the pedestrian
     * shall point towards.
     */
    protected abstract fun computeSubdestination(targetDoor: F): Euclidean2DPosition

    /**
     * Moves the pedestrian towards a position which is guaranteed to be in sight (i.e. no
     * obstacle is placed between him and such position).
     */
    protected open fun moveTowards(target: Euclidean2DPosition, currentRoom: M?, targetDoor: F): Unit =
        Seek(environment, this, pedestrian, *target.coordinates).execute()

    /**
     * Assign a weight to a given edge. The one with minimum weight will be chosen and crossed.
     * @param rank is the rank given to the edge when assessing its suitability to reach the
     * next sub-destination. See [cognitiveMapFactor].
     */
    protected open fun weight(edge: F, rank: Int?) = volatileMemoryFactor(edge) *
        cognitiveMapFactor(rank) * finalDestinationFactor(edge) * impasseFactor(edge)

    /*
     * This factor takes into account the information stored in the pedestrian's
     * volatile memory. It is computed as 2^k where k is the number of visits
     * to the area the edge being weighted leads to.
     */
    private fun volatileMemoryFactor(edge: F) = 2.0.pow(pedestrian.volatileMemory[edge.head] ?: 0)

    /*
     * This factor takes into account the information stored in the pedestrian's
     * cognitive map. It is computed as 1 - 0.5^r where r is the rank given to
     * the edge assessing its suitability to reach the next sub-destination.
     * If rank is null, this factor is 1 for every edge.
     */
    private fun cognitiveMapFactor(rank: Int?) = 1.0 - (rank?.let { 0.5.pow(it) } ?: 0.0)

    /*
     * This factor takes into account any final destination discovered along the way.
     */
    private fun finalDestinationFactor(edge: F) =
        if (environment.graph().containsAnyDestination(edge.head)) destinationWeight else 1.0

    /*
     * This factor takes into account whereas the assessed edge leads to an impasse or not.
     */
    private fun impasseFactor(edge: F) = if (isImpasse(edge.head)) 10.0 else 1.0

    /*
     * Registers a visit in the given area in the pedestrian's volatile memory.
     */
    private fun OrientingAgent<Euclidean2DPosition, A, N, *>.registerVisit(area: M) {
        volatileMemory[area] = (volatileMemory[area] ?: 0) + 1
    }

    /*
     * Checks if the pedestrian KNOWS that the given area is an impasse
     * (i.e. with a single door).
     */
    private fun isImpasse(area: M): Boolean =
        pedestrian.volatileMemory.contains(area) &&
            environment.graph().edgesFrom(area).map { it.head }.distinct().count() <= 1

    companion object {
        private const val destinationWeight = 0.1
    }
}
