package it.unibo.alchemist.groups

import it.unibo.alchemist.agents.Pedestrian

/**
 * An object representing a pedestrian alone.
 */
object NoGroup : Group {

    override val members: Set<Pedestrian<*>> = setOf()

    override fun contains(ped: Pedestrian<*>) = false
}