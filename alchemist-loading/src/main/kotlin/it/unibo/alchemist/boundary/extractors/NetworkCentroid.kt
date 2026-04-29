package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import kotlin.Double.Companion.NaN

/**
 * Exporter for the network centroid coordinates.
 * The x and y coordinates are the average of the x and y coordinates of all the nodes in the network,
 * note that negative coordinates are possible.
 */
class NetworkCentroid : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-centroid"
        const val ORIGIN = 0.0
    }

    override val columnNames: List<String>
        get() = listOf<String>("X", "Y", "Z").map { "$NAME-$it" }

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = when (environment.nodeCount.current) {
        0 -> columnNames.associateWith { NaN }
        else ->
            environment.networkHub().toList().mapIndexed { index, value ->
                columnNames[index] to value
            }.take(environment.dimensions).toMap()
    }

    private fun <T> Environment<T, *>.networkHub(): List<Double> {
        val sums = DoubleArray(dimensions) { ORIGIN }
        forEach { node ->
            getCurrentPosition(node).coordinates.forEachIndexed { index, value ->
                sums[index] += value
            }
        }
        return sums.map { it / nodeCount.current }
    }
}
