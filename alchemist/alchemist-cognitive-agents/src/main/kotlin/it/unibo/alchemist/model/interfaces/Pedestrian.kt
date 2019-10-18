package it.unibo.alchemist.model.interfaces

/**
 * A pedestrian with any characteristic taken into consideration.
 */
interface Pedestrian<T> : Node<T> {

    /**
     * The group this pedestrian belongs to.
     */
    val membershipGroup: PedestrianGroup<T>

    /**
     * The speed at which the pedestrian is moving.
     */
    fun speed(): Double
}