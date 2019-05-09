package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.homogeneous.Pedestrian
import it.unibo.alchemist.model.interfaces.Position

/**
 * Abstraction for a group of pedestrians
 */
class Group<T, P : Position<P>>(val members: MutableSet<Pedestrian<T, P>> = mutableSetOf()) {

    fun addPedestrian(ped: Pedestrian<T, P>) = members.add(ped)
}