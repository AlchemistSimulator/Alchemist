package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import org.danilopianini.util.ListBackedSet
import org.danilopianini.util.ListSet
import org.eclipse.xtend.lib.annotations.Accessors
import org.danilopianini.util.ListSets
import java.util.ArrayList

@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
class SimpleNeighborhood<T> implements Neighborhood<T> {

	val Environment<T> env
	val ListSet<Node<T>> neighbors
	val Node<T> center

	protected new(Environment<T> env, Node<T> center, Iterable<? extends Node<T>> neighbors) {
		this.env = env
		this.center = center
		this.neighbors = new ListBackedSet(new ArrayList(neighbors.toList))
	}

	override addNeighbor(Node<T> neigh) {
		neighbors.add(neigh)
	}

	override clone() {
		new SimpleNeighborhood(env, center, neighbors)
	}

	override contains(Node<T> n) { neighbors.contains(n) }

	override contains(int n) {
		neighbors.map[it.getId].exists[it == n]
	}

	override getBetweenRange(double min, double max) {
		val cpos = env.getPosition(center)
		new ListBackedSet(neighbors.filter[
			val d = cpos.getDistanceTo(env.getPosition(it))
			d < min || d > max
		].toList)
	}

	override getCenter() { center }

	override getNeighborById(int id) { neighbors.findFirst[it.id == id] }

	override getNeighborByNumber(int num) { neighbors.get(num) }

	override getNeighbors() { ListSets.unmodifiableListSet(neighbors) }

	override isEmpty() { neighbors.isEmpty }

	override removeNeighbor(Node<T> neighbor) { neighbors.remove(neighbor) }

	override size() { neighbors.size }

	override iterator() { neighbors.iterator }
	
	override toString() {
		'''«center» links: «neighbors»'''
	}


}