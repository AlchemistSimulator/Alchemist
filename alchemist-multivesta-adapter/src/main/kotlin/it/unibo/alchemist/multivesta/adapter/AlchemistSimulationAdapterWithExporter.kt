/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.loader.export.exporters.MultiVestaExporter.Companion.getValue

/**
 * This is an adapter that allow MultiVesta to interact with Alchemist.
 * @param simulation the simulation to be wrapped.
 */
class AlchemistSimulationAdapterWithExporter(
    private val simulation: Simulation<Any, Nothing>
) : AbstractAlchemistSimulationAdapter(simulation) {

    override fun getObsValue(obs: String): Double = obsValueToDouble(obs, getValue(simulation, obs))

    override fun getObsValue(obsId: Int): Double = obsValueToDouble(obsId, getValue(simulation, obsId))

    private fun obsValueToDouble(obs: Any, value: Any?): Double = checkNotNull(value) {
        "The observation value for $obs is null"
    }.toString().toDouble()
}
