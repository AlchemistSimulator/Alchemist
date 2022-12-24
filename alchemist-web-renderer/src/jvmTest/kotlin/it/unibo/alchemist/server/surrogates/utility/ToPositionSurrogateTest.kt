/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import io.kotest.assertions.fail
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.model.surrogate.GeneralPositionSurrogate
import it.unibo.alchemist.common.model.surrogate.Position2DSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.server.surrogates.utility.ToPositionSurrogate.toSuitablePositionSurrogate

class ToPositionSurrogateTest : StringSpec({

    "ToPositionSurrogate should map a Position to a GeneralPositionSurrogate" {
        val alchemistPosition = object : Position<Nothing> {
            override fun boundingBox(range: Double): List<Nothing> = error("Do not call, this is a test")
            override val coordinates: DoubleArray get() = doubleArrayOf(1.001, 2.004, 5.67, 1.0, 2.0)
            override fun getCoordinate(dimension: Int): Double = coordinates[dimension]
            override val dimensions: Int get() = 5
            override fun plus(other: DoubleArray): Nothing = error("Do not call, this is a test")
            override fun minus(other: DoubleArray): Nothing = error("Do not call, this is a test")
            override fun distanceTo(other: Nothing): Double = error("Do not call, this is a test")
        }
        val positionSurrogate = toSuitablePositionSurrogate(alchemistPosition.dimensions)(alchemistPosition)
        if (positionSurrogate is GeneralPositionSurrogate) {
            checkToPositionSurrogate(alchemistPosition, positionSurrogate)
        } else {
            fail("$positionSurrogate type is incorrect")
        }
    }

    "ToPositionSurrogate should map a Position2D to a Position2DSurrogate" {
        val alchemist2DPosition = Euclidean2DPosition(2.5, 5.8)
        val position2DSurrogate = toSuitablePositionSurrogate(alchemist2DPosition.dimensions)(alchemist2DPosition)
        if (position2DSurrogate is Position2DSurrogate) {
            checkToPositionSurrogate(alchemist2DPosition, position2DSurrogate)
        } else {
            fail("$position2DSurrogate type is incorrect")
        }
    }
})
fun <P : Position<out P>> checkToPositionSurrogate(position: P, positionSurrogate: PositionSurrogate) {
    position.coordinates shouldBe positionSurrogate.coordinates
    position.dimensions shouldBe positionSurrogate.dimensions
    if (position is Position2D<*> && positionSurrogate is Position2DSurrogate) {
        position.x shouldBe positionSurrogate.x
        position.y shouldBe positionSurrogate.y
    }
}
