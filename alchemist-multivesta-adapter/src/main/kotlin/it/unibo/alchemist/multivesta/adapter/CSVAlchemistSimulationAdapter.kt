/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

import it.unibo.alchemist.multivesta.adapter.utils.SeedsManager
import java.io.File

/**
 * This is a [AlchemistSimulationAdapter] that loads an already executed simulation's data from a CSV file.
 * The file is chosen by randomly selecting a seed from the currently available seeds list and then
 * by applying the [filenameForSeed] function to the selected seed.
 * The selected seed is then removed from the available seeds list.
 * @param seed the seed used to select the seed to use for the simulation,
 *        i.e. the seed given from MultiVesta
 * @param rootOutputFiles the root folder where the CSV files are stored
 * @param filenameForSeed the function that takes the selected seed and returns the filename
 *                        corresponding to the simulation CSV file to load
 *                        (the file must be in the [rootOutputFiles] folder)
 * @throws IllegalStateException if the available seeds list is empty
 */
open class CSVAlchemistSimulationAdapter(
    val seed: Int,
    val rootOutputFiles: File,
    filenameForSeed: (Int) -> String,
) : AlchemistSimulationAdapter {
    private val alchemistStateObservations: List<AlchemistStateObservations>
    private var time = 0
    init {
        val effectiveSeed = checkNotNull(SeedsManager.popNextAvailableSeed(seed)) {
            "MultiVesta cannot reached the requested confidence level with the given delta. " +
                "Try to increase the number of simulations or to decrease the delta/confidence."
        }
        val filename = filenameForSeed(effectiveSeed)
        val simulationFile = File(rootOutputFiles, filename)
        alchemistStateObservations = AlchemistSimStatesLoader.fromCSV(simulationFile.absolutePath)
    }

    override fun getTime(): Double = time.toDouble()

    override fun rval(obs: String): Double {
        return alchemistStateObservations[time].getObservation(obs)
    }

    override fun rval(obsId: Int): Double = alchemistStateObservations[time].getObservation(obsId)

    override fun doStep() {
        time++
    }

    override fun performWholeSimulation() {
        // Do nothing
    }
}
