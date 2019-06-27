package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * An object representing a pedestrian alone.
 */
object NoGroup : Group {

    override val members: Set<Pedestrian<*>> = setOf()

    override fun contains(ped: Pedestrian<*>) = false
}