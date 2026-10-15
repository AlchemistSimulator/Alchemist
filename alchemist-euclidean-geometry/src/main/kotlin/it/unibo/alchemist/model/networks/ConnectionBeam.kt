/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.environments.Euclidean2DEnvironmentWithObstacles
import it.unibo.alchemist.model.observation.Observable
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.PathIterator
import java.awt.geom.Rectangle2D
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.nextTowards
import kotlin.math.nextUp
import kotlin.math.sin

/**
 * Connects two nodes if, throwing a beam from one to the other, there exists at least one path entirely inside the
 * beam that connects the two nodes. This model is ideal for environments with obstacles, where the user wants some
 * tolerance in connection breaking.
 *
 * @param T concentration type
 * @param environment the environment whose nodes this model links
 * @param radius beam maximum length
 * @param beamWidth beam span (tolerance)
 */
class ConnectionBeam<T>(
    environment: Environment<T, Euclidean2DPosition>,
    radius: Double,
    private val beamWidth: Double,
) : ObservingRangeBasedNetworkModel<T, Euclidean2DPosition, Euclidean2DPosition>(environment, radius) {

    // The environment with obstacles is this same environment, holding nodes with the same concentration type.
    private val obstacleEnvironment = environment as? Euclidean2DEnvironmentWithObstacles<*, T>

    // The obstacles are collected when first needed, as they may be added after the network model gets created.
    private val obstacles: Area by lazy {
        Area().apply {
            obstacleEnvironment?.obstacles?.forEach { obstacle ->
                // Doubles are prone to approximation errors: enlarging the bounds by an ulp gets rid of them.
                val bounds = obstacle.bounds2D
                val minX = bounds.minX.nextTowards(NEGATIVE_INFINITY)
                val minY = bounds.minY.nextTowards(NEGATIVE_INFINITY)
                val maxX = bounds.maxX.nextUp()
                val maxY = bounds.maxY.nextUp()
                add(Area(Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY)))
            }
        }
    }

    override fun isLinked(
        center: Node<T>,
        centerInput: Euclidean2DPosition,
        neighbor: Node<T>,
        neighborInput: Euclidean2DPosition,
    ): Boolean = obstacleEnvironment?.let {
        !it.intersectsObstacle(centerInput, neighborInput) ||
            projectedBeamOvercomesObstacle(centerInput, neighborInput)
    } ?: true

    // Whether the beam overcomes the obstacles depends on the positions of both nodes.
    override fun observeInput(node: Node<T>): Observable<Euclidean2DPosition> = environment.positionOf(node)

    private fun projectedBeamOvercomesObstacle(from: Euclidean2DPosition, to: Euclidean2DPosition): Boolean {
        val angle = atan2(to.y - from.y, to.x - from.x)
        // The vertices surrounding the beam, and how much the beam is enlarged at its ends.
        val dx = beamWidth * cos(PI / 2 + angle)
        val dy = beamWidth * sin(PI / 2 + angle)
        val cx = beamWidth * cos(angle)
        val cy = beamWidth * sin(angle)
        val beam = Area(
            Path2D.Double().apply {
                moveTo(from.x + dx - cx, from.y + dy - cy)
                lineTo(from.x - dx - cx, from.y - dy - cy)
                lineTo(to.x - dx + cx, to.y - dy + cy)
                lineTo(to.x + dx + cx, to.y + dy + cy)
                closePath()
            },
        )
        beam.subtract(obstacles)
        // At least one of the areas the obstacles split the beam into must contain both points.
        return beam.subareas().any { it.contains(from.x, from.y) && it.contains(to.x, to.y) }
    }

    private companion object {
        const val COORDS = 6

        fun Area.subareas(): List<Path2D.Double> {
            val subareas = mutableListOf<Path2D.Double>()
            var current = Path2D.Double()
            val coordinates = DoubleArray(COORDS)
            val iterator = getPathIterator(null)
            while (!iterator.isDone) {
                when (iterator.currentSegment(coordinates)) {
                    PathIterator.SEG_MOVETO -> current = Path2D.Double().apply {
                        moveTo(coordinates[0], coordinates[1])
                    }
                    PathIterator.SEG_LINETO -> current.lineTo(coordinates[0], coordinates[1])
                    PathIterator.SEG_CLOSE -> {
                        current.closePath()
                        subareas += current
                    }
                    else -> throw IllegalArgumentException("Unexpected segment type in a polygonal area")
                }
                iterator.next()
            }
            return subareas
        }
    }
}
