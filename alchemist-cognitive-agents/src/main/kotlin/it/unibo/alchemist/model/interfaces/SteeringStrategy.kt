package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Position

/**
 * Strategy interface describing how the next points of the steering actions
 * are combined to calculate the next position to move on.
 */
interface SteeringStrategy<T, P : Position<P>> {

    /**
     * Computes the next position starting from the steering actions the node obey to,
     * in relative coordinates with respect to its current position.
     *
     * @param actions
     *          the list of actions to combine.
     */
    fun computeNextPosition(actions: List<SteeringAction<T, P>>): P

    /**
     * Computes the target to reach starting from the steering actions the node obey to,
     * in absolute coordinates.
     *
     * @param actions
     *          the list of actions to combine.
     */
    fun computeTarget(actions: List<SteeringAction<T, P>>): P
}
