/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

interface SimulationAdapter {
    fun getTime(): Double
    fun rval(obs: String): Double
    fun rval(obsId: Int): Double
    fun doStep()
    fun performWholeSimulation()
}
