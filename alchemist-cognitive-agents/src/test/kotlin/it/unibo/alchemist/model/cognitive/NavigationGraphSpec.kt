/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import com.uchuhimo.konf.ConfigSpec

object EnvironmentSpec : ConfigSpec() {
    val name by required<String>()
    val zoom by required<Double>()
    val startX by required<Double>()
    val startY by required<Double>()
    val width by required<Double>()
    val height by required<Double>()
}

object SeedsSpec : ConfigSpec() {
    val positions by required<List<List<Double>>>()
    val side by required<Double>()
}

object CrossingsSpec : ConfigSpec() {
    val side by required<Double>()
}
