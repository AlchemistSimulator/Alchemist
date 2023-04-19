/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContain
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.testsupport.createSimulation
import it.unibo.alchemist.testsupport.loadAlchemist
import it.unibo.alchemist.testsupport.runInCurrentThread

class SimpleFunctionalTest : StringSpec(
    {
        "the gradient should propagate" {
            val simulation = loadAlchemist<Any, Euclidean2DPosition>("gradient-on-a-line.yml")
                .createSimulation(finalStep = 100L)
                .runInCurrentThread()
            val valuesOfLastNode = simulation.environment.nodes.asSequence().find { it.id == 3 }?.contents?.values
            requireNotNull(valuesOfLastNode)
            valuesOfLastNode shouldContain 3.0
        }
    },
)
