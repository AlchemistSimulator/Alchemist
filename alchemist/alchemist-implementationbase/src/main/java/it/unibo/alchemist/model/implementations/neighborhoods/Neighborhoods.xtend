package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import java.util.Collections

final class Neighborhoods {

	private new() {}

	def public static <T> Neighborhood<T> make(Environment<T> env, Node<T> center) {
		make(env, center, Collections.emptyList)
	}

	def public static <T> Neighborhood<T> make(Environment<T> env, Node<T> center, Iterable<Node<T>> neighbors) {
		new SimpleNeighborhood(env, center, neighbors)
	}

}