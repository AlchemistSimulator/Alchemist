package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.Pedestrian

/**
 * Abstraction for a group of pedestrians
 */
class Group<T>(val members: MutableSet<Pedestrian<T>> = mutableSetOf()) {

    fun addPedestrian(ped: Pedestrian<T>) = members.add(ped)
}