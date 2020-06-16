/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.positions.LatLongPosition
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.model.interfaces.Position

val ll10: Position<GeoPosition> = LatLongPosition(1, 0)
val ll01: Position<GeoPosition> = LatLongPosition(0, 1)
val ll11: Position<GeoPosition> = LatLongPosition(1, 1)

class TestLatLongPosition : StringSpec({
    "LatLongPositions should sum correctly" {
        ll01 as GeoPosition + ll10 as GeoPosition shouldBe ll11
        ll10 + ll01 shouldBe ll11
        ll01 + ll10.coordinates shouldBe ll11
        ll10 + ll01.coordinates shouldBe ll11
    }
    "LatLongPositions should subtract correctly" {
        ll11 as GeoPosition - ll10 as GeoPosition shouldBe ll01
        ll11 - ll01 as GeoPosition shouldBe ll10
        ll11 - ll10.coordinates shouldBe ll01
        ll11 - ll01.coordinates shouldBe ll10
    }
})
