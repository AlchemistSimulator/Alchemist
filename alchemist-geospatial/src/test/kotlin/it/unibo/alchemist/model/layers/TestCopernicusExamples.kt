/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.acquisition.CopernicusRequest
import it.unibo.alchemist.boundary.utils.CopernicusInputs
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Loads the Copernicus examples shown on the website, written both in YAML and with the Kotlin DSL.
 */
class TestCopernicusExamples {

    @Test
    fun `in the local data example, nodes perceive the data of the directory`() {
        assertEquivalentExamplesPerceiveFirstSlice("copernicus/local-data")
    }

    @Test
    fun `the datastore example reads cached data without accessing the network`() {
        val request = CopernicusRequest(
            endpoint = "https://ewds.climate.copernicus.eu/api",
            dataset = "cems-glofas-historical",
            inputs = CopernicusInputs.read(resource("copernicus/glofas-request.json")),
        )
        // the cache already holds the requested data, so neither network nor credentials are needed
        val entry = Path.of("build/copernicus-cache").resolve(request.directoryName)
        entry.toFile().deleteRecursively()
        Files.createDirectories(entry)
        Files.copy(resource("copernicus/glofas-cesena/glofas-cesena.nc"), entry.resolve("glofas-cesena.nc"))
        assertEquivalentExamplesPerceiveFirstSlice("copernicus/datastore")
    }

    private fun assertEquivalentExamplesPerceiveFirstSlice(example: String) {
        val fromYaml = load("$example.yml")
        val fromKotlin = load("$example.alchemist.kts")
        assertEquals(fromYaml.nodePositions(), fromKotlin.nodePositions())
        assertNodesPerceiveFirstSlice(fromYaml)
        assertNodesPerceiveFirstSlice(fromKotlin)
    }

    private fun load(example: String): Environment<Any?, GeoPosition> =
        LoadAlchemist.from(resource(example).toUri().toURL()).getDefault<Any?, GeoPosition>().environment

    private fun Environment<Any?, GeoPosition>.nodePositions(): List<GeoPosition> =
        nodes.current.map { positionOf(it).current }

    private fun resource(name: String): Path =
        Path.of(requireNotNull(javaClass.classLoader.getResource(name)) { "Missing resource $name" }.toURI())

    private fun assertNodesPerceiveFirstSlice(environment: Environment<Any?, GeoPosition>) {
        val molecule = environment.incarnation.createMolecule("river-discharge")
        assertIs<DoubleCopernicusLayer>(environment.layerFor(molecule))
        assertTrue(environment.nodes.current.isNotEmpty())
        environment.nodes.current.forEach { node ->
            // at time zero, the value of the first slice of the data
            assertEquals(FIRST_SLICE_VALUE, environment.observeLayerValue(molecule, node).current)
        }
    }

    private companion object {
        const val FIRST_SLICE_VALUE = 10.0
    }
}
