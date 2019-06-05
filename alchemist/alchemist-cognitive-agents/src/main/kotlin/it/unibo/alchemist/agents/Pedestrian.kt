package it.unibo.alchemist.agents

import it.unibo.alchemist.groups.Group
import it.unibo.alchemist.model.interfaces.Node

/**
 * A pedestrian with any characteristic taken into consideration.
 */
interface Pedestrian<T> : Node<T> {

    /**
     * The group this pedestrian belongs to.
     */
    var membershipGroup: Group

    /**
     * The speed at which the pedestrian moves if it's walking.
     */
    val walkingSpeed: Double

    /**
     * The speed at which the pedestrian moves if it's running.
     */
    val runningSpeed: Double
}