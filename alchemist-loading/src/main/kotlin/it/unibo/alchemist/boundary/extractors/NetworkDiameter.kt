package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.util.Environments.networkDiameter

/**
 * Extractor for the [networkDiameter] by using the Euclidean distance within the simulation.
 * The network is assumed to be unsegmented,
 * otherwise use [SubnetworksDiameter].
 */
class NetworkDiameter : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-diameter"
    }

    override val columnNames: List<String> = listOf(NAME)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDiameter())
}
