package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

open class GenericGroup<T>(
    final override val members: List<Pedestrian<T>>
) : Group {

    init { members.forEach { it.membershipGroup = this } }
}