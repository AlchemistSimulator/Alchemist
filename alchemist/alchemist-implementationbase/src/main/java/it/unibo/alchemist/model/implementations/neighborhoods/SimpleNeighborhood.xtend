/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 * 
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.model.implementations.neighborhoods

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Neighborhood
import it.unibo.alchemist.model.interfaces.Node
import java.util.Iterator
import org.danilopianini.util.ImmutableListSet
import org.danilopianini.util.ListBackedSet
import org.eclipse.xtend.lib.annotations.Accessors

@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
class SimpleNeighborhood<T> implements Neighborhood<T> {

	val Environment<T, ?> env
	val ImmutableListSet<? extends Node<T>> neighbors
	val Node<T> center

	protected new(Environment<T, ?> env, Node<T> center, Iterable<? extends Node<T>> neighbors) {
		this.env = env
		this.center = center
		this.neighbors = new ImmutableListSet.Builder().addAll(neighbors).build
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

	override getNeighbors() { neighbors }

	override isEmpty() { neighbors.isEmpty }

	override size() { neighbors.size }

	override iterator() { neighbors.iterator as Iterator<Node<T>> }
	
	override toString() {
		'''«center» links: «neighbors»'''
	}
	
	override add(Node<T> node) {
		new SimpleNeighborhood(env, center, [new Iterator<Node<T>> {
			val previousNodes = neighbors.iterator
			var nodeReady = true
			override hasNext() { nodeReady }
			override next() {
				if (previousNodes.hasNext) {
					previousNodes.next
				} else {
					if (nodeReady){
						nodeReady = false
						node
					} else {
						throw new IllegalStateException("No other elements.")
					}
				}
			}
		}])
	}
	
	override remove(Node<T> node) {
		if (contains(node)) {
			new SimpleNeighborhood(env, center, [new Iterator<Node<T>> {
				val base = neighbors.iterator
				var Node<T> lookahead = updateLookAhead
				def Node<T> updateLookAhead() {
					if (base.hasNext){
						val maybeNext = base.next
						if (maybeNext == node) {
							updateLookAhead
						} else {
							maybeNext
						}
					} else {
						null
					}
				}
				override hasNext() { lookahead !== null }
				override next() {
					if (hasNext) {
						val result = lookahead
						lookahead = updateLookAhead
						result
					} else {
						throw new IllegalStateException("No other elements.")
					}
				}
			}])
		} else {
			throw new IllegalArgumentException('''«node» not in «this»''')
		}
	}


}
