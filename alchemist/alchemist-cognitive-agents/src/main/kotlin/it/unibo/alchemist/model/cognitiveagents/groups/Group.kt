package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * A generic group of pedestrians.
 */
interface Group<T> {

    /**
     * The list of pedestrians belonging to this group.
     */
    val members: List<Pedestrian<T>>

    /**
     * Whether a pedestrian belongs to this group or not.
     *
     * @param pedestrian The pedestrian to whom the membership must be checked.
     */
    fun contains(pedestrian: Pedestrian<T>): Boolean = members.contains(pedestrian)
}