package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * Basic implementation of a group.
 */
open class GenericGroup<T> : Group<T> {

    private val _members: MutableList<Pedestrian<T>> = mutableListOf()

    override val members: List<Pedestrian<T>>
        get() = _members

    override fun addMember(pedestrian: Pedestrian<T>): Group<T> = apply {
        if(!members.contains(pedestrian)) {
            _members.add(pedestrian)
        }
    }

    override fun removeMember(pedestrian: Pedestrian<T>): Group<T> = apply {
        _members.remove(pedestrian)
    }
}
