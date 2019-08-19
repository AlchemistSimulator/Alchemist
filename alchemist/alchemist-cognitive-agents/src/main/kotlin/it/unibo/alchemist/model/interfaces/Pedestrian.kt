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

    /**
     * A list of all the pedestrians inside at least one of the sensory spheres of this pedestrian.
     */
    fun influencialPeople(): List<Pedestrian<T>>
}