package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.Pedestrian

/**
 * Abstraction for a group of pedestrians
 */
interface Group {

    val members: Set<Pedestrian<*>>

    fun contains(ped: Pedestrian<*>): Boolean = members.contains(ped)
}