/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exporters.GlobalExporter
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Assertions
import org.kaikikm.threadresloader.ResourceLoader

class TestExportProperty<T, P : Position<P>> : StringSpec({
    "test export property" {
        val file = ResourceLoader.getResource("testExportProperty.yml")
        Assertions.assertNotNull(file)
        val loader = LoadAlchemist.from(file)
        Assertions.assertNotNull(loader)
        val simulation: Simulation<T, P> = loader.getDefault()
        val exporters = simulation.outputMonitors.filterIsInstance<GlobalExporter<T, P>>().flatMap { it.exporters }
        exporters.size shouldBe 1
        val exporter = exporters.first()
        require(exporter is CSVExporter) {
            "Invalid exporter of type '${exporter::class.simpleName}'"
        }
    }
})
