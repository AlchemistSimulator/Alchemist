package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

abstract class AbstractGroup<T>(
    final override val members: Set<Pedestrian<T>>
) : Group {

    init { members.forEach { it.membershipGroup = this } }
}