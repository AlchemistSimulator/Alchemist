package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A plain pedestrian.
 */
interface Pedestrian<T, S : Vector<S>, A : GeometricTransformation<S>> : NodeWithShape<T, S, A> {

    /**
     * The list of influence spheres belonging to this pedestrian (by default, only its [fieldOfView]).
     */
    val senses: Map<String, InfluenceSphere<T>> get() = mapOf("view" to fieldOfView)

    /**
     * The field of view of the pedestrian.
     */
    val fieldOfView: InfluenceSphere<T>

    /**
     * The group this pedestrian belongs to.
     */
    val membershipGroup: PedestrianGroup<T, S, A>

    /**
     * The speed at which the pedestrian is moving.
     */
    fun speed(): Double
}
