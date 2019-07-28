package it.unibo.alchemist.model.interfaces

/**
 * Strategy interface describing how the next points of the steering actions
 * are combined to calculate the next position to move on.
 */
interface SteeringStrategy<T, P : Position<P>> {

    /**
     * Computes the next position starting from the steering actions the pedestrian obey to.
     *
     * @param actions
     *          the list of actions to combine.
     */
    fun computePosition(actions: List<SteeringAction<T, P>>): P
}