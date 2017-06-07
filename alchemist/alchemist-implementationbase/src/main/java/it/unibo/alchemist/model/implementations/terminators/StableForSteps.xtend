package it.unibo.alchemist.model.implementations.terminators

import com.google.common.collect.Maps
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import java.util.Collections
import java.util.Map
import java.util.function.Predicate
import org.eclipse.xtend.lib.annotations.Accessors

@Accessors(PROTECTED_GETTER, PROTECTED_SETTER)
class StableForSteps<T> implements Predicate<Environment<T>> {
	
	var Map<Node<T>, Position> positions = Collections.emptyMap
	var Table<Node<T>, Molecule, T> contents = makeTable(0)
	val long interval
	val long intervals
	var long success = 0

	new(long checkInterval, long equalIntervals) {
		if (checkInterval <= 0 || equalIntervals <= 0) {
			throw new IllegalArgumentException("The intervals must be strictly positive")
		}
		interval = checkInterval
		intervals = equalIntervals
	}
	
	def private static <T> Table<Node<T>, Molecule, T> makeTable(int size) {
		Tables.newCustomTable(Maps.newLinkedHashMapWithExpectedSize(size), [Maps.newLinkedHashMapWithExpectedSize(size)])
	}
	
	override test(Environment<T> env) {
		if (env.simulation.step % interval == 0) {
			val newPositions = env.toMap([it], [env.getPosition(it)])
			val newContents = makeTable(env.nodesNumber)
			env.forEach[ node |
				node.contents.forEach[ molecule, concentration | 
					newContents.put(node, molecule, concentration)
				]
			]
			if (newPositions == positions && newContents == contents) {
				success++
			} else {
				success = 0
			}
			positions = newPositions
			contents = newContents
		}
		success == intervals
	}
	
}
