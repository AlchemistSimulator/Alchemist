package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.PedestrianGroup
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Group representing a pedestrian alone.
 */
class Alone<T, S : Vector<S>, A : GeometricTransformation<S>>(
    pedestrian: Pedestrian<T, S, A>
) : PedestrianGroup<T, S, A> {

    override val members = listOf(pedestrian)

    override fun addMember(node: Pedestrian<T, S, A>): PedestrianGroup<T, S, A> =
        throw UnsupportedOperationException()

    override fun removeMember(node: Pedestrian<T, S, A>): PedestrianGroup<T, S, A> =
        throw UnsupportedOperationException()
}
