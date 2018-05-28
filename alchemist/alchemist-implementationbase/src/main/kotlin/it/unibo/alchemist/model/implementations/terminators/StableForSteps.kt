package it.unibo.alchemist.model.implementations.terminators

import com.google.common.collect.Maps
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import java.util.function.Predicate

open class StableForSteps<T>(
    private val checkInterval: Long,
    private val equalIntervals: Long
) : Predicate<Environment<T>> {
    var success: Long = 0
    var positions: Map<Node<T>, Position> = emptyMap()
    var contents = makeTable<T>(0)

    init {
        if (checkInterval <= 0 || equalIntervals <= 0) {
            throw IllegalArgumentException("The intervals must be strictly positive")
        }
    }

    override fun test(environment: Environment<T>): Boolean {
        if (environment.simulation.step % checkInterval == 0L) {
            val newPositions = environment.associateBy({ it }, { environment.getPosition(it) })
            val newContents = makeTable<T>(environment.nodesNumber)
            environment.forEach { node ->
                node.contents.forEach { molecule, concentration ->
                    newContents.put(node, molecule, concentration)
                }
            }
            success = if (newPositions == positions && newContents == contents) success + 1 else 0
            positions = newPositions
            contents = newContents
        }
        return success == equalIntervals
    }

    companion object {
        private fun <T> makeTable(size: Int): Table<Node<T>, Molecule, T> =
                Tables.newCustomTable(Maps.newLinkedHashMapWithExpectedSize(size), { Maps.newLinkedHashMapWithExpectedSize<Molecule, T>(size) })
    }
}