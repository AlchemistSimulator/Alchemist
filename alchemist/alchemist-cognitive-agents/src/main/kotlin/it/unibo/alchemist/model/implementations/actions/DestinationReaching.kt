/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.orienting.AbstractEuclideanOrientingAction
import it.unibo.alchemist.model.interfaces.OrientingAction
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * An [OrientingAction] allowing to reach a destination.
 * The client can specify a list of [knownDestinations] (see [Pursuing]) and [unknownDestinations] (see
 * [GoalOrientedExploring]).
 * The pedestrian will try to reach the closest known destination for which a path leading there is known,
 * but in case another destination is found along the way (either known or unknown), the latter will be
 * approached instead of the chosen known destination. To put it in another way, this behavior mixes
 * [KnownDestinationReaching] and [GoalOrientedExploring].
 */
open class DestinationReaching<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    /**
     * Known destinations (can be empty, but if it is, [unknownDestinations] must not be empty).
     */
    private val knownDestinations: List<Euclidean2DPosition>,
    /**
     * Unknown destinations, defaults to an empty list.
     */
    private val unknownDestinations: List<Euclidean2DPosition> = emptyList()
) : GoalOrientedExploring<T, N, E>(
    environment,
    reaction,
    pedestrian,
    /*
     * This may seem strange, but as stated above if we found a destination along the way (either known
     * or unknown), we want to approach it and leave the route we're following.
     */
    knownDestinations + unknownDestinations
) {

    /**
     * Accepts a pair containing the known destinations in its first element and the unknown ones in the
     * second element.
     */
    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        destinations: Pair<List<Euclidean2DPosition>, List<Euclidean2DPosition>>
    ) : this(environment, reaction, pedestrian, destinations.first, destinations.second)

    /**
     * Accepts an array of numbers containing the destinations and uses [inferIsKnown] to partition them into
     * known and unknown ones.
     */
    constructor(
        environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
        reaction: Reaction<T>,
        pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
        vararg destinations: Number
    ) : this(
        environment,
        reaction,
        pedestrian,
        destinations.toPositions().partition { inferIsKnown(it, pedestrian, environment) }
    )

    init {
        require(knownDestinations.isNotEmpty() || unknownDestinations.isNotEmpty()) {
            "at least one destination (either known or unknown) has to be provided"
        }
    }

    /**
     * [KnownDestinationReaching] behavior is used via composition (can be null if [knownDestinations]
     * is empty).
     * TODO(using behaviors via composition is a pain in the neck, find a solution)
     */
    private val knownDestinationReaching: KnownDestinationReaching<T, N, E>? =
        knownDestinations.takeIf { it.isNotEmpty() }?.let {
            KnownDestinationReaching(environment, reaction, pedestrian, it)
        }

    override fun explore() = reachUnknownDestination {
        knownDestinationReaching?.let {
            /*
             * Removes duplicate visit.
             */
            pedestrian.unregisterVisit(currentRoomOrFail())
            /*
             * Synchronize states.
             */
            copyStateFrom(it)
        } ?: super.baseExplore()
    }

    override fun update() {
        super.update()
        /*
         * Keeps [knownDestinationReaching] up to date.
         */
        knownDestinationReaching?.update()
    }

    /**
     * Copies all the state variables from the [other] behavior (kind of memento pattern).
     * It's useful when orienting actions are used via composition.
     * TODO(use reflection, maybe?)
     */
    private fun copyStateFrom(other: AbstractEuclideanOrientingAction<T, N, E>) {
        cachedCurrentPosition = other.cachedCurrentPosition
        cachedCurrentRoom = other.cachedCurrentRoom
        state = other.state
        previousRoom = other.previousRoom
        crossingPoints = other.crossingPoints
        expectedNewRoom = other.expectedNewRoom
        finalDestination = other.finalDestination
    }
}

/**
 * Infers if a [destination] is known by the [pedestrian] (see [Pursuing]). A destination is considered
 * to be known if the pedestrian's cognitive map contains at least one landmark located in the same
 * room (= [environment]'s area) of the destination.
 */
fun <T, P, N : ConvexGeometricShape<P, *>> inferIsKnown(
    destination: P,
    pedestrian: OrientingPedestrian<T, P, *, N, *>,
    environment: EnvironmentWithGraph<*, T, P, *, out ConvexGeometricShape<P, *>, *>
): Boolean where P : Position<P>, P : Vector<P> =
    environment.graph.nodeContaining(destination)?.let { destinationArea ->
        pedestrian.cognitiveMap.vertexSet().any { destinationArea.contains(it.centroid) }
    } ?: false
