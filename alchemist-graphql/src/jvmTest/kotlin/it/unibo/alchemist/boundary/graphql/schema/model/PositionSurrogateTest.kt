/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.GenericPositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.Position2DSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.util.PositionSurrogateUtils
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D

class PositionSurrogateTest : StringSpec({
    "A two dimensional position should should be mapped to the correct surrogate" {
        val position2D = TestPosition(doubleArrayOf(1.0, 2.0))
        val positionSurrogate = PositionSurrogateUtils.toPositionSurrogate(position2D)
        if (positionSurrogate is Position2DSurrogate) {
            checkPositionSurrogate(position2D, positionSurrogate)
        } else {
            error("PositionSurrogate is not converted properly!")
        }
    }

    "A three dimensional position should should be mapped to the correct surrogate" {
        val genericPosition = TestPosition(doubleArrayOf(1.0, 2.0, 3.0))
        val positionSurrogate = PositionSurrogateUtils.toPositionSurrogate(genericPosition)
        if (positionSurrogate is GenericPositionSurrogate) {
            checkPositionSurrogate(genericPosition, positionSurrogate)
        } else {
            error("PositionSurrogate is not converted properly!")
        }
    }

    "InputPosition should behave as expected" {
        val position = TestPosition(doubleArrayOf(1.0, 2.0, 3.0))
        val inputPosition = PositionSurrogateUtils.toPositionSurrogate(position).toInputPosition()

        position.dimensions shouldBe inputPosition.dimensions
        position.coordinates shouldBe inputPosition.coordinates

        val positionSurrogate = PositionSurrogateUtils.fromPositionInput(inputPosition)
        checkPositionSurrogate(position, positionSurrogate)
    }
})

private class TestPosition(
    override val coordinates: DoubleArray,
    override val dimensions: Int = coordinates.size,
) : Position<Nothing> {
    override fun boundingBox(range: Double): List<Nothing> = error("Not implemented in tests")
    override fun getCoordinate(dimension: Int): Double = coordinates[dimension]
    override fun plus(other: DoubleArray): Nothing = error("Not implemented in tests")
    override fun minus(other: DoubleArray): Nothing = error("Not implemented in tests")
    override fun distanceTo(other: Nothing): Double = error("Not implemented in tests")
}

fun <P : Position<out P>>checkPositionSurrogate(position: P, positionSurrogate: PositionSurrogate) {
    position.dimensions shouldBe positionSurrogate.dimensions
    position.coordinates shouldBe positionSurrogate.coordinates

    if (position is Position2D<*> && positionSurrogate is Position2DSurrogate) {
        position.x shouldBe positionSurrogate.x
        position.y shouldBe positionSurrogate.y
    }
}
