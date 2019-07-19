package it.unibo.alchemist.model.influencesphere

import it.unibo.alchemist.model.interfaces.Node

/**
 * Area in which nodes exert/suffer an influence to/by other nodes.
 */
interface InfluenceSphere<T> {

    /**
     * The list of nodes relevant for this sphere of influence.
     */
    fun influentialNodes(): List<Node<T>>
}