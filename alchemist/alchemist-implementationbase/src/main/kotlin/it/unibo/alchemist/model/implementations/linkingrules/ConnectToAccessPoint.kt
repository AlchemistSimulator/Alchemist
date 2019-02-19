package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position

class ConnectToAccessPoint<T, P : Position<P>>(radius: Double, val accessPointId: Molecule)
    : ConnectWithinDistance<T, P>(radius) {

    private val Node<T>.isAccessPoint
        get() = contains(accessPointId)

    override fun computeNeighborhood(center: Node<T>, env: Environment<T, P>): Neighborhood<T> =
        super.computeNeighborhood(center, env).run {
            if (center.isAccessPoint) this else Neighborhoods.make(env, center, filter { it.isAccessPoint })
        }
}
