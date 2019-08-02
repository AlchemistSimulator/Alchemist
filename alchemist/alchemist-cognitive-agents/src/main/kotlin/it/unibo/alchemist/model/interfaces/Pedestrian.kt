package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.groups.Group

/**
 * A pedestrian with any characteristic taken into consideration.
 */
interface Pedestrian<T> : Node<T> {

    /**
     * The group this pedestrian belongs to.
     */
    fun membershipGroup(): Group<T>

    /**
     * Change the group this pedestrian belongs to.
     *
     * @param group
     *          the group this pedestrian starts to be part of.
     */
    fun changeMembershipGroup(group: Group<T>)

    /**
     * The speed at which the pedestrian is moving.
     */
    fun speed(): Double
}