package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import org.danilopianini.util.ListBackedSet
import org.danilopianini.util.ListSet

data class SimpleNeighborhood<T> internal constructor(
        private val env: Environment<T>,
        private val center: Node<T>,
        private val neighbors: ListSet<Node<T>>): Neighborhood<T> {

    internal constructor(env: Environment<T>, center: Node<T>, neighbors: Iterable<Node<T>>)
            : this(env, center, ListBackedSet(if (neighbors is MutableList) neighbors else neighbors.toMutableList()))

    override fun addNeighbor(neighbor: Node<T>?) {
        neighbors.add(neighbor)
    }

    override fun clone() = SimpleNeighborhood(env, center, neighbors)

    override fun contains(n: Int) = neighbors.map { it.id }.contains(n)

    override fun contains(n: Node<T>?) = neighbors.contains(n)

    override fun getBetweenRange(min: Double, max: Double): ListSet<Node<T>> {
        val centerPosition = env.getPosition(center)
        return ListBackedSet(neighbors.filter { centerPosition.getDistanceTo(env.getPosition(it)) in min..max })
    }

    override fun getCenter() = center

    override fun getNeighborById(id: Int) : Node<T> = neighbors.first { it.id == id }

    override fun getNeighborByNumber(num: Int): Node<T> = neighbors[num]

    override fun getNeighbors(): ListSet<Node<T>> = neighbors

    override fun isEmpty() = neighbors.isEmpty()

    override fun iterator() = neighbors.iterator()

    override fun removeNeighbor(neighbor: Node<T>?) {
        neighbors.remove(neighbor)
    }

    override fun size() = neighbors.size

    override fun toString() = "$center links: $neighbors"

}