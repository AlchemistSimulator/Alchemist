/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.navigation

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.cognitive.OrientingProperty
import it.unibo.alchemist.model.cognitive.actions.NavigationAction2D
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition

/**
 * [Pursue] strategy allowing to dynamically change [destination].
 *
 * @param T the concentration type.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 */
open class DynamicallyPursue<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    override var destination: Euclidean2DPosition,
) : Pursue<T, L, R>(action, destination) {

    /**
     * Changes the destination of the strategy. If [voidVolatileMemory] is true, the node's
     * volatile memory is set to zero. This has two effects:
     * - known impasses remain stored (hence the node will keep avoiding them)
     * - rooms visited while pursuing the previous destination won't be penalised (= won't be avoided)
     * Defaults to false.
     */
    fun setDestination(newDestination: Euclidean2DPosition, voidVolatileMemory: Boolean = false) {
        destination = newDestination
        action.currentRoom?.let {
            /*
             * If the node is inside a room, we force the re-computation of what to do. Otherwise
             * he/she's crossing a door and inNewRoom will be called as soon as a room is reached.
             */
            inNewRoom(it)
        }
        if (voidVolatileMemory) {
            /*
             * clear() would cause volatileMemory[anyArea] to be null, which in turn means the node would
             * forget known impasses as well (an impasse is known when volatileMemory[area] != null). Setting
             * volatileMemory[anyArea] to zero allows to remember known impasses.
             */
            node.asProperty<T, OrientingProperty<T, *, *, *, *, *>>().volatileMemory.replaceAll { _, _ -> 0 }
        }
    }
}
