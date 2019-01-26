package it.unibo.alchemist.model.implementations.linkingrules

import it.unibo.alchemist.model.implementations.neighborhoods.Neighborhoods
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position

class ConnectViaAccessPoint<T, P : Position<P>>(radius: Double, val accessPointId: Molecule)
    : ConnectWithinDistance<T, P>(radius) {

    private val Node<T>.isAccessPoint
        get() = contains(accessPointId)

    private fun Neighborhood<T>.closestAccessPoint(env: Environment<T, P>): Node<T>? =
            asSequence().filter { it.isAccessPoint }.minBy { env.getDistanceBetweenNodes(center, it) }

    override fun computeNeighborhood(center: Node<T>, env: Environment<T, P>): Neighborhood<T> =
        super.computeNeighborhood(center, env).run {
            if (!center.isAccessPoint) {
                // Connect to closest access point and all nodes connected to the same AP
                closestAccessPoint(env)?.let { closestAP ->
                    Neighborhoods.make(env, center, neighbors
                        .filter { it == closestAP || !it.isAccessPoint && env.getNeighborhood(it).contains(closestAP) }
                    )
                } ?: Neighborhoods.make(env, center, emptyList())
            } else {
                if (all { it.isAccessPoint }) {
                    this
                } else {
                    // The AP must connect only if it is the closest AP of each node or the node is not connected
                    Neighborhoods.make(env, center, neighbors.asSequence()
                        .filter { it.isAccessPoint || env.getNeighborhood(it).run {
                            contains(center) || none { it.isAccessPoint }
                        } }
                        .asIterable()
                    )
                }
            }
        }
}