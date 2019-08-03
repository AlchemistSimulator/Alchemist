package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.groups.Group

/**
 * A pedestrian with any characteristic taken into consideration.
 */
interface Pedestrian<T> : Node<T> {

    /**
     * The group this pedestrian belongs to.
     */
    val membershipGroup: Group<T>

    /**
     * The speed at which the pedestrian is moving.
     */
    fun speed(): Double
}