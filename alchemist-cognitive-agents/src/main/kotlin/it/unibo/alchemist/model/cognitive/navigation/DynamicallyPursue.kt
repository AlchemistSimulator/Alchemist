/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.geometry.ConvexPolygon
import it.unibo.alchemist.model.geometry.Euclidean2DConvexShape
import it.unibo.alchemist.model.geometry.navigationgraph.Euclidean2DPassage
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [Pursue] strategy variant that allows changing the destination dynamically.
 *
 * @param T the concentration type.
 * @param L the landmark shape type used by the node's cognitive map.
 * @param R the relation/edge type used by the node's cognitive map.
 * @param action the navigation action backing this strategy.
 * @property destination the initial destination position.
 */
open class DynamicallyPursue<T, L : Euclidean2DConvexShape, R>(
    action: NavigationAction2D<T, L, R, ConvexPolygon, Euclidean2DPassage>,
    override var destination: Euclidean2DPosition,
) : Pursue<T, L, R>(action, destination) {
    /**
     * Changes the destination of the strategy. If [voidVolatileMemory] is true, the node's
     * volatile memory is reset to zero while preserving known impasses.
     *
     * @param newDestination the new destination position.
     * @param voidVolatileMemory whether to reset volatile memory (defaults to false).
     */
    fun setDestination(newDestination: Euclidean2DPosition, voidVolatileMemory: Boolean = false) {
        destination = newDestination
        action.currentRoom?.let {
            /*
             * If the node is inside a room, we force the re-computation of what to do. Otherwise
             * the node is crossing a door and inNewRoom will be called as soon as a room is reached.
             */
            inNewRoom(it)
        }
        if (voidVolatileMemory) {
            /*
             * clear() would cause volatileMemory[anyArea] to be null, which in turn means the node would
             * forget known impasses as well (an impasse is known when volatileMemory[area] != null). Setting
             * volatileMemory[anyArea] to zero allows remembering known impasses while resetting visit penalties.
             */
            node.asProperty<T, OrientingProperty<T, *, *, *, *, *>>().volatileMemory.replaceAll { _, _ -> 0 }
        }
    }
}
