/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.Time
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.POSITIVE_INFINITY
import kotlin.math.max
import kotlin.math.min

/**
 * Extractor for the network density of the environment.
 * The network density is the average number of nodes per unit area.
 * The area is the rectangle given by the outermost nodes in the network.
 */
class NetworkDensity : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-density"
    }

    override val columnNames: List<String> = listOf(NAME)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDensity())

    private fun <T> Environment<T, *>.networkDensity(): Double {
        data class BoundingBox(
            val minX: Double = POSITIVE_INFINITY,
            val maxX: Double = NEGATIVE_INFINITY,
            val minY: Double = POSITIVE_INFINITY,
            val maxY: Double = NEGATIVE_INFINITY,
        )

        val boundingBox = nodes.fold(BoundingBox()) { bb, node ->
            val (x, y) = getCurrentPosition(node).coordinates
            BoundingBox(
                min(x, bb.minX),
                max(x, bb.maxX),
                min(y, bb.minY),
                max(y, bb.maxY),
            )
        }
        val area = (boundingBox.maxX - boundingBox.minX) * (boundingBox.maxY - boundingBox.minY)
        return when {
            area <= 0 || area.isInfinite() -> Double.NaN
            else -> nodeCount.current / area
        }
    }
}
