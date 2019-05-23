package it.unibo.alchemist.agents

import it.unibo.alchemist.groups.Group
import it.unibo.alchemist.model.interfaces.Node

/**
 * A pedestrian with neither individual nor cognitive characteristics taken into consideration.
 */
interface Pedestrian<T> : Node<T> {

    var membershipGroup: Group
}