/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.environments

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.biochemistry.BiochemistryIncarnation
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.physics.environments.AbstractLimitedContinuous2D
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.slf4j.LoggerFactory

/**
 * A rectangular environment for cells, bounded by [minX], [maxX], [minY], and [maxY].
 * Inconsistent bounds (a maximum not greater than its minimum) fall back to -1 and 1 for all bounds.
 */
open class BioRect2DEnvironment(
    incarnation: BiochemistryIncarnation,
    minX: Double,
    maxX: Double,
    minY: Double,
    maxY: Double,
) : AbstractLimitedContinuous2D<Double>(incarnation) {
    private val consistent = maxX > minX && maxY > minY
    private val minX = if (consistent) minX else -1.0
    private val maxX = if (consistent) maxX else 1.0
    private val minY = if (consistent) minY else -1.0
    private val maxY = if (consistent) maxY else 1.0

    init {
        if (!consistent) {
            LOGGER.warn(
                "A maximum bound for this environment is greather than the correspoding minimum bound. " +
                    "Falling back to -1, 1 for all bounds",
            )
        }
    }

    /**
     * Builds an unbounded environment.
     */
    constructor(incarnation: BiochemistryIncarnation) : this(
        incarnation,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY,
        Double.NEGATIVE_INFINITY,
        Double.POSITIVE_INFINITY,
    )

    final override fun next(curX: Double, curY: Double, newX: Double, newY: Double): Euclidean2DPosition =
        Euclidean2DPosition(newX.coerceIn(minX, maxX), newY.coerceIn(minY, maxY))

    final override fun isAllowed(position: Euclidean2DPosition): Boolean =
        position.x > minX && position.x < maxX && position.y > minY && position.y < maxY

    /**
     * Only cells move. After a cell moves, its junctions with nodes that are no longer neighbors are removed.
     */
    override fun moveNodeTo(node: Node<Double>, position: Euclidean2DPosition) {
        val cell = node.asPropertyOrNull<Double, CellProperty<*>>() ?: return
        super.moveNodeTo(node, position)
        val neighborhood = getNeighborhood(node).current
        // Iterate over copies, since removing junctions changes the junction maps.
        cell.junctions.current.toList().forEach { (junction, linkedNodes) ->
            linkedNodes.current.toList()
                .filter { (linkedNode, _) -> linkedNode !in neighborhood }
                .forEach { (linkedNode, count) ->
                    repeat(count) {
                        cell.removeJunction(junction, linkedNode)
                        linkedNode.asProperty<Double, CellProperty<*>>().removeJunction(junction.reverse(), node)
                    }
                }
        }
    }

    private companion object {
        private val LOGGER = LoggerFactory.getLogger(BioRect2DEnvironment::class.java)
    }
}
