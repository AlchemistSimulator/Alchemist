/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time

/**
 * This is an adapter that allow MultiVesta to interact with Alchemist.
 * @param simulation the simulation to be wrapped.
 */
class AlchemistSimulationAdapterWithExtractor(
    private val simulation: Simulation<Any, Nothing>,
    /**
     * The extractor to be used to extract data from the simulation.
     */
    val extractor: Extractor<Any>,
) : AbstractAlchemistSimulationAdapter(simulation) {

    private var lastReaction: Actionable<Any>? = null

    init {
        simulation.addOutputMonitor(object : OutputMonitor<Any, Nothing> {
            override fun stepDone(
                environment: Environment<Any, Nothing>,
                reaction: Actionable<Any>?,
                time: Time,
                step: Long,
            ) {
                lastReaction = reaction
            }
        })
    }

    override fun getObsValue(obs: String): Double = extractData()[obs]
        ?: throw IllegalArgumentException("Observation $obs not found in the extractor")

    override fun getObsValue(obsId: Int): Double = when {
        obsId < 0 -> throw IllegalArgumentException("Observation id $obsId is negative")
        obsId == 0 -> simulation.time.numeric()
        else -> extractData().entries.elementAt(obsId).value
    }

    private fun extractData(): Map<String, Double> = extractor
        .extractData(simulation.environment, lastReaction, simulation.time, simulation.step)
        .mapValues { it.value.toString().toDouble() }

    private inline fun <reified T : Number> Any.numeric(): T = when {
        this is T -> this
        this is Number -> when (T::class) {
            Int::class -> toInt()
            Double::class -> toDouble()
            Long::class -> toLong()
            else -> TODO()
        } as T
        else -> TODO()
    }
}
