package it.unibo.alchemist.model.cognitiveagents.groups

import it.unibo.alchemist.model.interfaces.Pedestrian

/**
 * An object representing a pedestrian alone.
 */
object NoGroup : Group {

    override val members: List<Pedestrian<*>> = listOf()

    override fun contains(ped: Pedestrian<*>) = false
}