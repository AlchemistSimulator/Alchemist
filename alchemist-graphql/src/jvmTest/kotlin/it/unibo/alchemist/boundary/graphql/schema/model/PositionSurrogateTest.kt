/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.schema.model

import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.GenericPositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.Position2DSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.PositionSurrogate
import it.unibo.alchemist.boundary.graphql.schema.util.PositionSurrogateUtils
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Position2D
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PositionSurrogateTest {

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `A two dimensional position should be mapped to the correct surrogate`() {
        val position2D = TestPosition(doubleArrayOf(1.0, 2.0))
        val positionSurrogate = PositionSurrogateUtils.toPositionSurrogate(position2D)
        assertTrue(positionSurrogate is Position2DSurrogate)
        checkPositionSurrogate(position2D, positionSurrogate)
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `A three dimensional position should be mapped to the correct surrogate`() {
        val genericPosition = TestPosition(doubleArrayOf(1.0, 2.0, 3.0))
        val positionSurrogate = PositionSurrogateUtils.toPositionSurrogate(genericPosition)
        assertTrue(positionSurrogate is GenericPositionSurrogate)
        checkPositionSurrogate(genericPosition, positionSurrogate)
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.MINUTES)
    fun `InputPosition should behave as expected`() {
        val position = TestPosition(doubleArrayOf(1.0, 2.0, 3.0))
        val inputPosition = PositionSurrogateUtils.toPositionSurrogate(position).toInputPosition()
        assertEquals(position.dimensions, inputPosition.dimensions, "Dimensions mismatch")
        assertEquals(position.coordinates.toList(), inputPosition.coordinates.toList(), "Coordinates mismatch")
        val positionSurrogate = PositionSurrogateUtils.fromPositionInput(inputPosition)
        checkPositionSurrogate(position, positionSurrogate)
    }
}

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

fun <P : Position<out P>> checkPositionSurrogate(position: P, positionSurrogate: PositionSurrogate) {
    assertEquals(position.dimensions, positionSurrogate.dimensions, "Surrogate dimensions mismatch")
    assertEquals(
        position.coordinates.toList(),
        positionSurrogate.coordinates.toList(),
        "Surrogate coordinates mismatch",
    )

    if (position is Position2D<*> && positionSurrogate is Position2DSurrogate) {
        assertEquals(position.x, positionSurrogate.x, "X coordinate mismatch in 2D surrogate")
        assertEquals(position.y, positionSurrogate.y, "Y coordinate mismatch in 2D surrogate")
    }
}
