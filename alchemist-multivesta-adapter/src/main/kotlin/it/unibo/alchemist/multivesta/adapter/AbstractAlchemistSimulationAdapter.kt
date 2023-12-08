/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

/**
 * This is an adapter that allow MultiVesta to interact with Alchemist.
 * @param simulation the simulation to be wrapped.
 */
abstract class AbstractAlchemistSimulationAdapter(
    private val simulation: Simulation<Any, Nothing>,
) : AlchemistSimulationAdapter {

    private val logger = LoggerFactory.getLogger(AbstractAlchemistSimulationAdapter::class.java)

    final override fun getTime(): Double {
        return simulation.time.toDouble()
    }

    final override fun rval(obs: String): Double = when (obs) {
        "time" -> getTime()
        "step" -> simulation.step.toDouble()
        else -> getObsValue(obs)
    }

    final override fun rval(obsId: Int): Double = getObsValue(obsId)

    final override fun doStep() {
        val nextTime = simulation.time.plus(DoubleTime(1.0))
        waitForTime(nextTime)
        logger.info("Step done. Now at time ${simulation.time}. Step ${simulation.step}.")
    }

    final override fun performWholeSimulation() {
        simulation.goToStep(simulation.finalStep)
    }

    /**
     * Get the value of the observation with the given id.
     */
    abstract fun getObsValue(obsId: Int): Double

    /**
     * Get the value of the observation with the given name.
     * The observations named "time" and "step" have been already handled.
     */
    abstract fun getObsValue(obs: String): Double

    private fun waitForTime(time: Time) {
        // to do: change implementation when simulation.goToTime() will be fixed
        while (simulation.time < time) {
            simulation.goToTime(time)
            simulation.play()
            simulation.waitFor(Status.RUNNING, AlchemistMultiVesta.MAX_WAIT_SECONDS, TimeUnit.SECONDS)
            simulation.waitFor(Status.PAUSED, AlchemistMultiVesta.MAX_WAIT_SECONDS, TimeUnit.SECONDS)
        }
    }
}
