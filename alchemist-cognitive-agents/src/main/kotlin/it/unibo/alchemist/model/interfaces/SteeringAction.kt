package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Position

/**
 * Action whose purpose is moving a node inside the environment it is in.
 */
interface SteeringAction<T, P : Position<P>> : Action<T> {

    /**
     * The position the owner of this action moves to when it is executed,
     * in relative coordinates with respect to its current position.
     */
    fun nextPosition(): P
}
