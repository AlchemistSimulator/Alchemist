/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractMoveNode
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.CircularCellProperty
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Moves a cell, namely a node with a [CellProperty], along its polarization versor, then resets the versor.
 *
 * The cell moves by [requestedDelta], or, if [inPercent] is true, by [requestedDelta] times its diameter;
 * a movement in percent requires a [CircularCellProperty] with a non-zero radius.
 */
class CellMove(
    environment: Environment<Double, Euclidean2DPosition>,
    reaction: NodeReaction<Double>,
    private val inPercent: Boolean,
    private val requestedDelta: Double,
) : AbstractMoveNode<Double, Euclidean2DPosition>(environment, reaction) {
    private val cell: CellProperty<Euclidean2DPosition> =
        requireNotNull(targetNode.asPropertyOrNull<Double, CellProperty<Euclidean2DPosition>>()) {
            "CellMove can be setted only in cells."
        }

    private val delta: Double = if (inPercent) {
        val circularCell = cell as? CircularCellProperty
        require(circularCell != null && circularCell.radius != 0.0) {
            "Can't set distance in percent of the cell's diameter if cell has not a diameter"
        }
        circularCell.diameter * requestedDelta
    } else {
        requestedDelta
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): CellMove =
        CellMove(environment, newReaction, inPercent, requestedDelta)

    override fun getNextPosition(): Euclidean2DPosition = Euclidean2DPosition(
        delta * cell.polarizationVersor.getCoordinate(0),
        delta * cell.polarizationVersor.getCoordinate(1),
    )

    override fun execute() {
        super.execute()
        cell.polarizationVersor = environment.makePosition(0, 0)
    }
}
