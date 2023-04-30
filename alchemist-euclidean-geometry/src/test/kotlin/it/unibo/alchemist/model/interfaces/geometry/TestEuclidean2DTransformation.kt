/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.geometry.Euclidean2DShapeFactory
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.shapes.AdimensionalShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.TestEuclidean2DShapeFactory.Companion.oneOfEachWithSize
import it.unibo.alchemist.model.positions.Euclidean2DPosition

private val factory: Euclidean2DShapeFactory =
    GeometricShapeFactory.getInstance<Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>()

@SuppressFBWarnings("SE_BAD_FIELD_STORE")
class TestEuclidean2DTransformation : FreeSpec({
    "Test origin" - {
        factory.oneOfEachWithSize(1.0)
            .filter { it.value !is AdimensionalShape }
            .forEach {
                it.key {
                    var shape = it.value.transformed {
                        origin(0.0, 0.0)
                    }
                    val reference = shape.centroid
                    shape = shape.transformed {
                        origin(0.0, 0.0)
                    }
                    reference.distanceTo(shape.centroid) shouldBe 0.0
                    shape = shape.transformed {
                        origin(0.0, 10.0)
                    }
                    reference.distanceTo(shape.centroid) shouldBe 10.0
                    shape = shape.transformed {
                        origin(10.0, 10.0)
                        origin(3.0, 3.0)
                        origin(6.0, 0.0)
                    }
                    reference.distanceTo(shape.centroid) shouldBe 6.0
                }
            }
    }
})
