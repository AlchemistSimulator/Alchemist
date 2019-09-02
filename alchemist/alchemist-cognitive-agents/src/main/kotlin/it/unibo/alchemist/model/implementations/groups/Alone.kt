package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Group
import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * Group representing a pedestrian alone.
 */
class Alone<T>(pedestrian: Pedestrian<T>) : Group<T, Pedestrian<T>> {

    override val members = listOf(pedestrian)

    override fun addMember(node: Pedestrian<T>): Group<T, Pedestrian<T>> = throw UnsupportedOperationException()

    override fun removeMember(node: Pedestrian<T>): Group<T, Pedestrian<T>> = throw UnsupportedOperationException()
}