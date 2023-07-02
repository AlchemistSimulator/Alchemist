/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

object Debug {

    @JvmStatic
    fun main(args: Array<String>) {
        Alchemist.main(
            arrayOf(
                "-y",
                "simulation.yml",
                "-g",
                "alchemist-benchmark/effects/simulation.json",
                "-t",
                "50",
                "-f",
                "mode=epsilon,epsilon=0.01,outputReplayStrategy=aggregate",
            ),
        )
    }
}
