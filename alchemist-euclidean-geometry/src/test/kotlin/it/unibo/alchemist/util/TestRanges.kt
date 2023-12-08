/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geometry.euclidean2d.Segments.coords
import it.unibo.alchemist.util.Ranges.coincidesWith
import it.unibo.alchemist.util.Ranges.findExtremeCoordsOnX
import it.unibo.alchemist.util.Ranges.findExtremeCoordsOnY

class TestRanges : StringSpec() {

    init {
        "test findExtremeCoords" {
            val list = listOf(
                coords(1.0, 1.0),
                coords(2.0, 2.0),
                coords(3.0, 2.0),
                coords(-1.0, 30.0),
            )
            list.findExtremeCoordsOnX().coincidesWith(-1.0..3.0) shouldBe true
            list.findExtremeCoordsOnY().coincidesWith(1.0..30.0) shouldBe true
        }
    }
}
