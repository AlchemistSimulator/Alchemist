package it.unibo.alchemist.model.interfaces

/**
 * Action whose purpose is moving a node inside the environment it is in.
 */
interface SteeringAction<T, P : Position<P>> : Action<T> {

    /**
     * The position the owner of this action moves to when it is executed.
     */
    fun nextPosition(): P

    /**
     * The position the owner of this action moves towards.
     */
    fun target(): P
}
