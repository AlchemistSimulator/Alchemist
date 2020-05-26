/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions.navigationstrategies

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.EuclideanNavigationAction
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * [Pursuing] strategy allowing to dynamically change [destination].
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 */
open class DynamicPursuing<T, N : Euclidean2DConvexShape, E>(
    action: EuclideanNavigationAction<T, N, E, ConvexPolygon, Euclidean2DPassage>,
    override var destination: Euclidean2DPosition
) : Pursuing<T, N, E>(action, destination) {

    /**
     * Changes the destination of the strategy. If [voidVolatileMemory] is true, the pedestrian's
     * volatile memory is set to zero. This has two effects:
     * - known impasses remain stored (hence the pedestrian will keep avoiding them)
     * - rooms visited while pursuing the previous destination won't be penalised (= won't be avoided)
     * Defaults to false.
     */
    fun setDestination(newDestination: Euclidean2DPosition, voidVolatileMemory: Boolean = false) {
        destination = newDestination
        action.currentRoom?.let {
            /*
             * If the pedestrian is inside a room, we force the re-computation of what to do. Otherwise
             * he/she's crossing a door and inNewRoom will be called as soon as a room is reached.
             */
            inNewRoom(it)
        }
        if (voidVolatileMemory) {
            /*
             * clear() would cause volatileMemory[anyArea] to be null, which in turn means the pedestrian would
             * forget known impasses as well (an impasse is known when volatileMemory[area] != null). Setting
             * volatileMemory[anyArea] to zero allows to remember known impasses.
             */
            pedestrian.volatileMemory.replaceAll { _, _ -> 0 }
        }
    }
}
