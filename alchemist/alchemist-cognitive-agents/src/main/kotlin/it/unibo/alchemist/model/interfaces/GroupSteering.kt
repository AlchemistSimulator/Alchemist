package it.unibo.alchemist.model.interfaces

/**
 * Steering action caused by a group of pedestrians.
 */
interface GroupSteering<T, P : Position<P>> : SteeringAction<T, P> {

    /**
     * The list of pedestrians used to compute the group steering action.
     */
    fun group(): List<Pedestrian<T>>
}