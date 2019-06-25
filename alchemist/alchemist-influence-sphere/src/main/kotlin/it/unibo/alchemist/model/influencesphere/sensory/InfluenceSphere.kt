package it.unibo.alchemist.model.influencesphere.sensory

import it.unibo.alchemist.model.interfaces.Position

/**
 * A sphere of influence in any n-dimensional space.
 */
interface InfluenceSphere<P : Position<P>> {

    /**
     * Whenever or not a given point has an influence on the node equipped with this sphere.
     */
    fun isInfluenced(point: P): Boolean
}