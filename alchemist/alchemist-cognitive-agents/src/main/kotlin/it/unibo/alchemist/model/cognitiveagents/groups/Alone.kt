package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * Group representing a pedestrian alone.
 */
class Alone<T>(pedestrian: Pedestrian<T>) : Group<T> {

    override val members = listOf(pedestrian)

    override fun addMember(pedestrian: Pedestrian<T>): Group<T> = throw UnsupportedOperationException()

    override fun removeMember(pedestrian: Pedestrian<T>): Group<T> = throw UnsupportedOperationException()
}