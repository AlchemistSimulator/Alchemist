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
        Alchemist.main(arrayOf("run", "simulation.yml", "--launcher", "it.unibo.alchemist.boundary.swingui.impl.SingleRunSwingUI", "--options", "alchemist-benchmark/src/jmh/resources/options.yml"))
    }
}
