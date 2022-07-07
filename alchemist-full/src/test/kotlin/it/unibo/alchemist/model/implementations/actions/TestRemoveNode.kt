/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import io.kotest.core.spec.style.StringSpec
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.testsupport.createSimulation
import it.unibo.alchemist.testsupport.loadAlchemist
import it.unibo.alchemist.testsupport.runInCurrentThread

class TestRemoveNode<T, P : Position<P>> : StringSpec({
    "Nodes can be removed in a continous 2D space" {
        val simulation = loadAlchemist<T, P>("remove.yml").createSimulation()
        val simulationRun = simulation.runInCurrentThread()
        assert(simulationRun.error.isEmpty) { "The simulation fail with ${simulationRun.error.get()}" }
    }
})
