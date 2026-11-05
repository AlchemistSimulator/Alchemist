/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.layers

import it.unibo.alchemist.boundary.LoadAlchemist
import it.unibo.alchemist.boundary.copernicus.CopernicusRequest
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.toPath
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
    fun `the datastore example with inline inputs reads cached data without accessing the network`() {
        fillCache()
        assertEquivalentExamplesPerceiveFirstSlice("copernicus/datastore")
    }

    @Test
    fun `the datastore example with inputs from the classpath reads cached data without accessing the network`() {
        fillCache()
        assertEquivalentExamplesPerceiveFirstSlice("copernicus/datastore-classpath")
    }

    private companion object {
        const val FIRST_SLICE_VALUE = 10.0

        /**
         * Puts the data of the datastore examples in their cache, so that neither network nor credentials are needed.
         * Examples whose inputs differ from `glofas-request.json` miss the cache, and fail trying to download.
         */
        private fun fillCache() {
            val request = CopernicusRequest(
                endpoint = "https://ewds.climate.copernicus.eu/api",
                dataset = "cems-glofas-historical",
                // the content of glofas-request.json, as parsed from JSON
                inputs = mapOf(
                    "system_version" to listOf("version_4_0"),
                    "hydrological_model" to listOf("lisflood"),
                    "product_type" to listOf("consolidated"),
                    "variable" to listOf("river_discharge_in_the_last_24_hours"),
                    "hyear" to listOf("2024"),
                    "hmonth" to listOf("06"),
                    "hday" to listOf("10"),
                    "data_format" to "netcdf",
                    "download_format" to "unarchived",
                    "area" to listOf(44.2, 12.2, 44.1, 12.3),
                ),
            )
            val entry = Path("build/copernicus-cache", request.directoryName)
            entry.toFile().deleteRecursively()
            entry.createDirectories()
            resource("copernicus/glofas-cesena/glofas-cesena.nc").copyTo(entry.resolve("glofas-cesena.nc"))
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

        private fun resource(name: String): Path =
            requireNotNull(javaClass.classLoader.getResource(name)) { "Missing resource $name" }.toURI().toPath()

        private fun assertNodesPerceiveFirstSlice(environment: Environment<Any?, GeoPosition>) {
            val molecule = environment.incarnation.createMolecule("river-discharge")
            assertIs<DoubleGriddedGeoLayer>(environment.layerFor(molecule))
            assertTrue(environment.nodes.current.isNotEmpty())
            environment.nodes.current.forEach { node ->
                // at time zero, the value of the first slice of the data
                assertEquals(FIRST_SLICE_VALUE, environment.observeLayerValue(molecule, node).current)
            }
        }

        fun Environment<Any?, GeoPosition>.nodePositions(): List<GeoPosition> =
            nodes.current.map { positionOf(it).current }
    }
}
