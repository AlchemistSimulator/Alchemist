package it.unibo.alchemist.model.implementations.groups

import it.unibo.alchemist.model.interfaces.GroupWithLeader
import it.unibo.alchemist.model.interfaces.Pedestrian

class Family<T>(
    comparator: Comparator<Pedestrian<T>>
) : GenericGroup<T, Pedestrian<T>>(), GroupWithLeader<T, Pedestrian<T>> {

    override val leader: Pedestrian<T> = members.minWith(comparator) ?: throw IllegalStateException("Can't determine a leader.")
}
