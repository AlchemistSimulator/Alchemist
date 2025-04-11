package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time

/**
 * Exporter for the network centroid coordinates.
 * The x and y coordinates are the average of the x and y coordinates of the nodes in the network,
 * note that negative coordinates are possible.
 */
class NetworkCentroid : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-centroid"
    }

    override val columnNames: List<String>
        get() = listOf<String>("$NAME-xCoord", "$NAME-yCoord")

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> =
        environment.networkHub().toList().mapIndexed { index, value -> "$NAME@$index" to value }.toMap()

    private fun <T> Environment<T, *>.networkHub(): Pair<Double, Double> = fold(0.0 to 0.0) { acc, next ->
        val nodePos = this.getPosition(next).coordinates // Note that negative positions can occur
        acc.first + nodePos[0] to acc.second + nodePos[1]
    }.let { sum ->
        sum.first / this.nodeCount to sum.second / this.nodeCount
    }
}
