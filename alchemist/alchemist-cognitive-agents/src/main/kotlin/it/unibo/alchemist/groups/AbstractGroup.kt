package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.Pedestrian

abstract class AbstractGroup<T>(final override val members: Set<Pedestrian<T>>) : Group {

    init {
        members.forEach { it.membershipGroup = this }
    }
}