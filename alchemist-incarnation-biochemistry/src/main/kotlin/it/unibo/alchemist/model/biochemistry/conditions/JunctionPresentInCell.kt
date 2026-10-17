/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import it.unibo.alchemist.model.observation.Observable

/**
 * A condition valid when [junction] is present in the cell.
 */
class JunctionPresentInCell(
    private val environment: Environment<Double, *>,
    reaction: NodeReaction<Double>,
    private val junction: Junction,
) : AbstractNeighborCondition<Double>(environment, reaction) {
    private val cell: CellProperty<*> = requireNotNull(targetNode.asPropertyOrNull<Double, CellProperty<*>>()) {
        "This Condition can be set only in node with ${CellProperty::class.simpleName}"
    }

    init {
        setValidity(cell.observeContainsJunction(junction))
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): JunctionPresentInCell =
        JunctionPresentInCell(environment, newReaction, junction)

    override fun observeNeighborWeight(neighbor: Node<Double>): Observable<Double> =
        cell.junctions[junction].switchMap { maybeJunctions ->
            maybeJunctions.fold(
                ifEmpty = { observe(0.0) },
                ifSome = { junctions ->
                    if (junctions.current.isEmpty()) {
                        observe(0.0)
                    } else {
                        junctions[neighbor].map { count -> count.fold(ifEmpty = { 0.0 }, ifSome = Int::toDouble) }
                    }
                },
            )
        }

    override fun toString(): String = "junction $junction present "
}
