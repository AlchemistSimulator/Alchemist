/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * A [Pursuing] behavior allowing to dynamically change [destination].
 */
open class DynamicPursuing<T, N : Euclidean2DConvexShape, E>(
    environment: Euclidean2DEnvironmentWithGraph<*, T, ConvexPolygon, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>,
    override var destination: Euclidean2DPosition
) : Pursuing<T, N, E>(environment, reaction, pedestrian, destination) {

    /**
     * Changes the destination of the behavior. If [voidVolatileMemory] is true, the pedestrian's
     * volatile memory is set to zero. This has two effects:
     * - known impasses remain stored (hence the pedestrian will keep avoiding them)
     * - rooms visited while pursuing the previous destination won't be penalised (= won't be avoided)
     * Defaults to false.
     */
    fun setDestination(newDestination: Euclidean2DPosition, voidVolatileMemory: Boolean = false) {
        destination = newDestination
        if (state >= State.MOVING_TO_FINAL) {
            state = State.NEW_ROOM
            /*
             * Unregister duplicate visit.
             */
            pedestrian.unregisterVisit(currentRoomOrFail())
        }
        if (voidVolatileMemory) {
            pedestrian.volatileMemory.replaceAll { _, _ -> 0 }
        }
    }
}
