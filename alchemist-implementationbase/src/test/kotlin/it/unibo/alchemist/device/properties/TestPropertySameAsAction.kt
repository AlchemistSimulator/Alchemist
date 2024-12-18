/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.device.properties

import io.kotest.core.spec.style.FreeSpec
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.test.AlchemistTesting.runInCurrentThread
import org.kaikikm.threadresloader.ResourceLoader

class TestPropertySameAsAction : FreeSpec({
    "The node of the property must be the same as the node of the action" {
        val simulation: Simulation<Nothing, Nothing> =
            LoadAlchemist.from(ResourceLoader.getResource("propertySameAsAction.yml")).getDefault()
        simulation.runInCurrentThread()
        simulation.error.ifPresent { throw it }
    }
})
