/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter.multivesta

import it.unibo.alchemist.multivesta.adapter.AlchemistMultiVesta.launchSimulation
import it.unibo.alchemist.multivesta.adapter.SimulationAdapter
import vesta.mc.NewState
import vesta.mc.ParametersForState

/**
 * This class exemplifies how to integrate a simulator in MultiVeStA
 * This class is the 'connection point' among the simulator and MultiVeStA.
 * It exposes to MultiVeStA the methods necessary to interact with the simulator.
 * The constructor is invoked once per MultiVeStA analysis.
 * It should take care of things necessary to be done only once independently of
 * the number of simulations or simulation steps performed.
 * @param parameters commented within the method
 */
class AlchemistSimState(parameters: ParametersForState) : NewState(parameters) {
    /**
     * An object representing the actual simulator
     */
    private lateinit var alchemistSimulator: SimulationAdapter

    /**
     * This method is invoked before starting every new simulation.
     * @param randomSeed    the seed to be used to initialize the random number
     *                      generator of the simulator for the new simulation
     */
    override fun setSimulatorForNewSimulation(randomSeed: Int) {
        // Necessary to reset the simulator to its initial state
        //  e.g., reset timers/counters, empty data structures, etc
        // BEWARE: the parameter randomSeed must be used to reset the random number generator of the simulator
        //         If this is not the case, there are no guarantees on the genuinity of the results computed by MultiVeStA
        alchemistSimulator = launchSimulation(randomSeed)
    }

    /**
     * This method is invoked to perform a single step of simulation. Most simulators perform
     * simulations by having a loop, where each iteration takes care of advancing one step.
     * This method corresponds to one iteration of such loop.
     */
    override fun performOneStepOfSimulation() {
        alchemistSimulator.doStep()
    }

    /**
     * This method returns the 'current simulated time' of the current state of the simulation
     * The notion of 'simulated time' depends on the nature of the simulator. It can be just
     * a counter of steps of simulation, or a some other notion computed by the simulator.
     */
    override fun getTime(): Double = alchemistSimulator.getTime()

    /**
     * This method asks the simulator to evaluate a given observation 'obs' in the current simulation state
     * What an observation of interest is depends on the considered simulator.
     * It can be
     * - a 0/1 property: 'did a given event or condition happen?', returning 0 or 1
     * - any real property: 'how many people escaped?' 'how long is the queue'?
     */
    override fun rval(obs: String): Double = alchemistSimulator.rval(obs)

    /**
     * This is the same as the other rval method.
     * The difference is that here we don't pass the 'name' of an observation, but its ID.
     * It can lead to slightly more efficient analysis, but makes writing queries more cumbersome.
     */
    override fun rval(obsID: Int): Double = alchemistSimulator.rval(obsID)

    // Advanced topic to be discussed later on
    /**
     * If a simulator has a notion of 'final state' (i.e., every simulation terminates when a given condition is met),
     * and we are not interested in values from intermediate states, we cdo not need to control every step of simulation.
     * Rather, we can ask the simulator to go on untile simulation is completed.
     */
    override fun performWholeSimulation() {
        // TODO Add here code to perform one whole simulation
    }
}
