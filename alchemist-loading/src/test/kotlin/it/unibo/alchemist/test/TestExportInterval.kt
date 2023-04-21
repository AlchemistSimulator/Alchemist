/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.CSVExporter
import it.unibo.alchemist.model.Position
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader

class TestExportInterval<T, P : Position<P>> : StringSpec({
    "test export interval" {
        val file = ResourceLoader.getResource("testExportInterval.yml")
        assertNotNull(file)
        val loader = LoadAlchemist.from(file)
        assertNotNull(loader)
        val initialized: InitializedEnvironment<T, P> = loader.getDefault()
        initialized.exporters.size shouldBe 1
        val exporter = initialized.exporters.first()
        require(exporter is CSVExporter) {
            "Invalid exporter of type '${exporter::class.simpleName}'"
        }
        exporter.interval shouldBe 3.0
        exporter.interval::class shouldBe Double::class
    }
})
