package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.GroupWithLeader
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [Family] is modeled as a group of pedestrians with a leader.
 */
class Family<T, S : Vector<S>, A : GeometricTransformation<S>>(
    comparator: Comparator<Pedestrian<T, S, A>> = Comparator { a, b -> a.id.compareTo(b.id) }
) : GenericGroup<T, Pedestrian<T, S, A>>(),
    GroupWithLeader<T, Pedestrian<T, S, A>> {

    override val leader: Pedestrian<T, S, A> =
        members.minWithOrNull(comparator) ?: throw IllegalStateException("Can't determine a leader.")
}
