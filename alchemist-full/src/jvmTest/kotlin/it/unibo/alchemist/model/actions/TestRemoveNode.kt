/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemist
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread

class TestRemoveNode<T, P : Position<P>> : StringSpec({
    "Nodes can be removed in a continous 2D space" {
        val simulation = loadAlchemist<T, P>("remove.yml")
        val simulationRun = simulation.runInCurrentThread()
        require(simulationRun.error.isEmpty) {
            throw simulationRun.error.get()
        }
    }
})
