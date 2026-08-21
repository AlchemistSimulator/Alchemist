/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.layers

import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import it.unibo.alchemist.TestVariable
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
import it.unibo.alchemist.model.maps.positions.LatLongPosition
import it.unibo.alchemist.writeTestNetcdf
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempDirectory

class TestOsmCopernicusCompatibility : StringSpec({

    // known positions inside maps/cesena.pbf
    val pharmacyPos = LatLongPosition(44.14022881997589, 12.234464874617203)
    val stadiumPos = LatLongPosition(44.140937161857074, 12.261716117329186)

    val incarnation = mockk<Incarnation<Any?, GeoPosition>>()

    "OSMEnvironment .pbf import and DoubleCopernicusLayer must coexist" {
        // tries to import an actual .pbf file and use it
        val environment = OSMEnvironment(incarnation, "maps/cesena.pbf")
        val route = environment.computeRoute(pharmacyPos, stadiumPos)
        route.points.size shouldBeGreaterThan 1

        // forces cdm-core to be exercised by creating a Copernicus layer
        val dir = createTempDirectory("pbf-copernicus")
        try {
            val fixedMeasurement = 42.0
            writeTestNetcdf(
                path = dir.resolve("data.nc"),
                lats = doubleArrayOf(44.0, 45.0, 46.0),
                lons = doubleArrayOf(11.0, 12.0, 13.0),
                timeHours = doubleArrayOf(0.0),
                variables = listOf(TestVariable(rawValues = DoubleArray(9) { fixedMeasurement })),
            )
            val layer = DoubleCopernicusLayer(environment, dir.absolutePathString())
            withClue("pharmacy is within the synthetic grid's bounding box") {
                layer.getValue(pharmacyPos) shouldBe fixedMeasurement
            }
        } finally {
            dir.toFile().deleteRecursively()
        }
    }
})
