package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A plain pedestrian.
 */
interface Pedestrian<T, S : Vector<S>, A : GeometricTransformation<S>> : NodeWithShape<T, S, A> {

    /**
     * The group this pedestrian belongs to.
     */
    val membershipGroup: PedestrianGroup<T, S, A>

    /**
     * The speed at which the pedestrian is moving.
     */
    fun speed(): Double
}
