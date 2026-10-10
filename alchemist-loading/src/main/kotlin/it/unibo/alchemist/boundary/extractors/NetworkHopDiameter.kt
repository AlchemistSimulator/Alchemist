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
        reaction: Reaction<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> = mapOf(NAME to environment.networkDiameterByHopDistance())
}
