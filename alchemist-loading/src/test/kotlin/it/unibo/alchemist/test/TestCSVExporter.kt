/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.InitializedEnvironment
import it.unibo.alchemist.loader.LoadAlchemist
import it.unibo.alchemist.loader.export.exporters.CSVExporter
import it.unibo.alchemist.loader.export.exporters.GlobalExporter
import it.unibo.alchemist.model.interfaces.Position
import org.junit.jupiter.api.Assertions.assertNotNull
import org.kaikikm.threadresloader.ResourceLoader
import java.io.File

class TestCSVExporter<T, P : Position<P>> : StringSpec({
    "test exporting data on CSV file" {
        val initialized: InitializedEnvironment<T, P> = loadAlchemist("testCSVExporter.yml")
        runSimulationUsingExporters(initialized)
        initialized.exporters.size shouldBe 1
        val exporter = getCSVExporter(initialized)
        val outputFile = File(exporter.exportPath)
            .listFiles()
            ?.find { it.name.startsWith("00-testing_csv_export_") && it.extension == exporter.fileExtension }
        outputFile.shouldNotBeNull()
        outputFile.shouldExist()
    }
    "test on column alignment" {
        val initialized: InitializedEnvironment<T, P> = loadAlchemist("testCSVExportColumnAlignment.yml")
        runSimulationUsingExporters(initialized)
        val exporter = getCSVExporter(initialized)
        // Get the first line of the output produce by CSVExporter
        val exporterFirstLine = File(exporter.exportPath)
            .listFiles()?.first { it.name.startsWith("column-alignment") }
            ?.readLines()
            ?.dropWhile { !it.contains("d c b a") } // remove the lines before the column names
            ?.drop(1) // I am not interested in column head
            ?.first()
        exporterFirstLine.shouldNotBeNull()
        exporterFirstLine.shouldNotBeEmpty()
        exporterFirstLine.shouldContain("0 1 2 3")
    }
}) {
    /* common utility functions */
    companion object {
        fun <T, P : Position<P>> loadAlchemist(simulationFile: String): InitializedEnvironment<T, P> {
            val file = ResourceLoader.getResource(simulationFile)
            assertNotNull(file)
            val loader = LoadAlchemist.from(file)
            assertNotNull(loader)
            return loader.getDefault()
        }
        fun <T, P : Position<P>> runSimulationUsingExporters(env: InitializedEnvironment<T, P>) {
            val simulation = Engine(env.environment)
            simulation.addOutputMonitor(GlobalExporter(env.exporters))
            simulation.play()
            simulation.run()
        }
        fun <T, P : Position<P>> getCSVExporter(env: InitializedEnvironment<T, P>): CSVExporter<T, P> {
            val exporter = env.exporters.first()
            require(exporter is CSVExporter) {
                "Invalid exporter type '${exporter::class.simpleName}'"
            }
            return exporter
        }
    }
}
