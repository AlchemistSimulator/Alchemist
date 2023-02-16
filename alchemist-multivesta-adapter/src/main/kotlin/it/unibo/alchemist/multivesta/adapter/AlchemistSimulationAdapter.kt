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
import it.unibo.alchemist.core.interfaces.Status
import it.unibo.alchemist.multivesta.adapter.exporter.MultiVestaExporter.Companion.getValue
import java.util.concurrent.TimeUnit

class AlchemistSimulationAdapter(private val simulation: Simulation<*, *>) : SimulationAdapter {
    override fun getTime(): Double {
        return simulation.step.toDouble()
    }

    override fun rval(obs: String): Double = obsValueToDouble(getValue(simulation, obs))

    override fun rval(obsId: Int): Double = obsValueToDouble(getValue(simulation, obsId))

    override fun doStep() {
        simulation.goToStep(simulation.step + 1)
        simulation.waitFor(Status.PAUSED, 1, TimeUnit.SECONDS)
    }

    override fun performWholeSimulation() {
        simulation.goToStep(simulation.finalStep)
    }

    private fun obsValueToDouble(value: Any?): Double = when (value) {
        null -> 0.0
        else -> {
            try {
                value.toString().toDouble()
            } catch (e: Exception) {
                0.0
            }
        }
    }
}
