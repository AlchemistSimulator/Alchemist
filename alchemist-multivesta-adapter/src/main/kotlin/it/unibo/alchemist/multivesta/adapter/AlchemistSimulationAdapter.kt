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
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.multivesta.adapter.exporter.MultiVestaExporter.Companion.getValue
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * This is an adapter that allow MultiVesta to interact with Alchemist.
 * @param simulation the simulation to be wrapped.
 */
class AlchemistSimulationAdapter(private val simulation: Simulation<Any, *>) : SimulationAdapter {

    private val logger = LoggerFactory.getLogger(AlchemistSimulationAdapter::class.java)

    override fun getTime(): Double {
        return simulation.time.toDouble()
    }

    override fun rval(obs: String): Double = when (obs) {
        "time" -> getTime()
        else -> obsValueToDouble(getValue(simulation, obs))
    }

    override fun rval(obsId: Int): Double = obsValueToDouble(getValue(simulation, obsId))

    override fun doStep() {
        val nextTime = simulation.time.plus(DoubleTime(1.0))
        logger.info("Time ${simulation.time}")
        waitForTime(nextTime)
        logger.info("Step done. Now at time ${simulation.time}. Step ${simulation.step}.")
    }

    private fun waitForTime(time: Time) {
        // TODO: change implementation when simulation.goToTime() will be fixed
        while (simulation.time < time) {
            simulation.goToTime(time)
            simulation.play()
            simulation.waitFor(Status.RUNNING, AlchemistMultiVesta.MAX_WAIT_SECONDS, TimeUnit.SECONDS)
            simulation.waitFor(Status.PAUSED, AlchemistMultiVesta.MAX_WAIT_SECONDS, TimeUnit.SECONDS)
        }
    }

    override fun performWholeSimulation() {
        simulation.goToStep(simulation.finalStep)
    }

    private fun obsValueToDouble(value: Any?): Double = when (value) {
        null -> 0.0
        else -> {
            try {
                value.toString().toDouble()
            } catch (e: NumberFormatException) {
                0.0
            }
        }
    }
}
