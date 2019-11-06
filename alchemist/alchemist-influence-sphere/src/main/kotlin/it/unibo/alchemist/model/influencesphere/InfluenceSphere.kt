package it.unibo.alchemist.model.influencesphere

import it.unibo.alchemist.model.interfaces.Node

/**
 * Area inside which nodes exert an influence on each other.
 */
interface InfluenceSphere {

    /**
     * The list of nodes relevant for this sphere of influence.
     */
    fun influentialNodes(): List<Node<*>>
}
