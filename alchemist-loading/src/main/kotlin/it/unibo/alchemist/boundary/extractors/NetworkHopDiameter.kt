package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.util.Environments.networkDiameterByHopDistance

/**
 * Extracts the diameter of the network by hop distance.
 */
class NetworkHopDiameter : Extractor<Double> {
    private companion object {
        private const val NAME: String = "network-hop-diameter"
    }

    override val columnNames: List<String> = listOf(NAME)

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDiameterByHopDistance())
}
