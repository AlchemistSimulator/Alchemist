package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.Pedestrian

/**
 * A generic group of pedestrians.
 */
interface Group {

    /**
     * The list of pedestrians belonging to this group.
     */
    val members: Set<Pedestrian<*>>

    /**
     * Whether a pedestrian belongs to this group or not.
     *
     * @param ped The pedestrian to whom the membership must be checked.
     */
    fun contains(ped: Pedestrian<*>): Boolean = members.contains(ped)
}