/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.model.GeoPosition
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.kaikikm.threadresloader.ResourceLoader

class TestAISSimulationLoading :
    StringSpec({
        "5min AIS NMEA trace should load into a simulation" {
            assumeTrue(
                ResourceLoader.getResourceAsStream(AIS_TRACE).bufferedReader().use { it.readText().isNotBlank() },
                "$AIS_TRACE is empty",
            )
            val simulation =
                LoadAlchemist
                    .from(ResourceLoader.getResource(AIS_SIMULATION))
                    .getDefault<Nothing, GeoPosition>()
            simulation.environment.nodeCount.current shouldBeGreaterThan 0
            simulation.play()
            simulation.run()
            simulation.error.ifPresent { throw it }
        }
    })

private const val AIS_SIMULATION = "simulations/ais-5min.yml"
private const val AIS_TRACE = "trace/ok/5min.nmea.txt"
