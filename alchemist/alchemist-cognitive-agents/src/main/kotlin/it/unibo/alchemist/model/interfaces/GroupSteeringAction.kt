package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Steering action caused by a group of pedestrians.
 */
interface GroupSteeringAction<T, P> : SteeringAction<T, P>
    where P : Position<P>, P : Vector<P> {

    /**
     * The list of pedestrians used to compute the group steering action.
     */
    fun group(): List<Pedestrian<T, P, *>>
}
