/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.matchers.doubles.shouldBeExactly
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.model.implementations.geometry.DoubleInterval
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.subtract
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Tests intersections.
 */
class TestIntervalsUtils {

    @Test
    fun testCreation() {
        val minusOneToFive = DoubleInterval(5.0, -1.0)
        minusOneToFive.first shouldBeExactly -1.0
        minusOneToFive.second shouldBeExactly 5.0
    }

    private fun assertIntersectionNull(first: Double, second: Double, third: Double, fourth: Double) =
        assertNull(DoubleInterval(first, second).intersection(DoubleInterval(third, fourth)))

    private fun assertIntersectionIs(
        first: Double,
        second: Double,
        third: Double,
        fourth: Double,
        intersectionFirst: Double,
        intersectionSecond: Double
    ) {
        val intersection = DoubleInterval(first, second).intersection(DoubleInterval(third, fourth))
        assertNotNull(intersection)
        intersection?.let {
            it.first shouldBeExactly intersectionFirst
            it.second shouldBeExactly intersectionSecond
        }
    }

    @Test
    fun testIntersection() {
        assertIntersectionIs(-1.0, 5.0, 2.0, 3.0, 2.0, 3.0)
        assertIntersectionIs(-1.0, 5.0, -2.0, 6.0, -1.0, 5.0)
        assertIntersectionIs(-1.0, 5.0, -2.0, 3.0, -1.0, 3.0)
        assertIntersectionNull(-1.0, 5.0, -3.0, -2.0)
        assertIntersectionIs(-1.0, 5.0, 3.0, 6.0, 3.0, 5.0)
        assertIntersectionNull(-1.0, 5.0, 6.0, 7.0)
    }

    private fun assertSubtractionIs(
        first: Double,
        second: Double,
        third: Double,
        fourth: Double,
        vararg expected: Double
    ) {
        DoubleInterval(first, second).subtract(DoubleInterval(third, fourth))
            .flatMap { mutableListOf(it.first, it.second) }
            .toDoubleArray() shouldBe expected
    }

    @Test
    fun testSubtraction() {
        assertSubtractionIs(-1.0, 5.0, 2.0, 3.0, -1.0, 2.0, 3.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -2.0, 6.0)
        assertSubtractionIs(-1.0, 5.0, -2.0, 3.0, 3.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -3.0, -2.0, -1.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, 3.0, 6.0, -1.0, 3.0)
        assertSubtractionIs(-1.0, 5.0, 6.0, 7.0, -1.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -1.0, 3.0, 3.0, 5.0)
    }
}
