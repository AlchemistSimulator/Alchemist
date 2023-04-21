/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.terminators

import com.google.common.collect.Maps
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import java.util.function.Predicate

/**
 * A [Predicate] that [tests][test] if an environment's
 * nodes (meaning their position and concentration) have
 * remained unchanged for a certain amount of steps.
 *
 * The check isn't performed on every [step][it.unibo.alchemist.core.interfaces.Simulation.getStep]
 * of a [simulation][it.unibo.alchemist.core.interfaces.Simulation],
 * instead an interval that determines how many steps are
 * to be skipped between each check is specified.
 * For [test] to return true, an environment must remain unchanged for
 * `checkInterval * equalIntervals` steps. This result might not be
 * entirely consistent, since the check isn't performed every step so
 * as not to cause performance issues. Therefore it might happen that
 * some changes occur in the environment but are reverted before the
 * next check is performed.
 *
 * [test] should be called at every step of the simulation in order
 * to avoid missing checks.
 *
 * @constructor Creates a new [StableForSteps] with the given values.
 *
 * @param checkInterval The recurrence of the test
 * @param equalIntervals The amount of [checkInterval] intervals that need to pass
 * (during which the environment doesn't change) for [test] to return true
 */
class StableForSteps<T>(
    private val checkInterval: Long,
    private val equalIntervals: Long,
) : Predicate<Environment<T, *>> {
    private var success: Long = 0
    private var positions: Map<Node<T>, Position<*>> = emptyMap()
    private var contents = makeTable<T>(0)

    init {
        require(checkInterval > 0 && equalIntervals > 0) {
            "The intervals must be strictly positive"
        }
    }

    override fun test(environment: Environment<T, *>): Boolean {
        if (environment.getSimulation().getStep() % checkInterval == 0L) {
            val newPositions = environment.associateBy({ it }, { environment.getPosition(it) })
            val newContents = makeTable<T>(environment.getNodeCount())
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
            Tables.newCustomTable(Maps.newLinkedHashMapWithExpectedSize<Node<T>, Map<Molecule, T>>(size)) {
                Maps.newLinkedHashMapWithExpectedSize(size)
            }
    }
}
