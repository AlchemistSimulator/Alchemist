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
import io.kotest.matchers.shouldNotBe
import it.unibo.alchemist.util.Ranges.coincidesWith
import it.unibo.alchemist.util.Ranges.contains
import it.unibo.alchemist.util.Ranges.intersect
import it.unibo.alchemist.util.Ranges.intersects
import it.unibo.alchemist.util.Ranges.intersectsBoundsExcluded
import it.unibo.alchemist.util.Ranges.minus
import it.unibo.alchemist.util.Ranges.rangeFromUnordered
import it.unibo.alchemist.util.Ranges.subtractAll
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

/**
 * Contains tests concerning [ClosedRange] extension functions.
 */
class TestRangeExtension : StringSpec({

    infix fun <T : Comparable<T>> ClosedRange<T>?.rangeShouldBe(other: ClosedRange<T>) {
        this shouldNotBe null
        this as ClosedRange<T>
        start shouldBe other.start
        endInclusive shouldBe other.endInclusive
    }

    "test rangeFromUnordered" {
        rangeFromUnordered(1.0, -1.0) rangeShouldBe -1.0..1.0
        rangeFromUnordered(-1.0, 1.0) rangeShouldBe -1.0..1.0
    }

    "test range containment" {
        (-1.0..1.0).contains(0.0..0.0) shouldBe true
        (-2.0..2.0).contains(0.0..0.0) shouldBe true
        (-1.0..1.0).contains(-1.0..1.0) shouldBe true
        (-1.0..0.9).contains(-1.0..1.0) shouldBe false
        (-1.5..-1.0).contains(0.0..0.0) shouldBe false
    }

    "test intersection" {
        (1..5).intersects(-3..2) shouldBe true
        (1..5).intersects(4..6) shouldBe true
        (1..5).intersects(2..4) shouldBe true
        (1..5).intersects(-3..6) shouldBe true
        (1..5).intersects(1..5) shouldBe true
        (1..5).intersects(-5..-3) shouldBe false
        (1..5).intersects(6..8) shouldBe false
        (1..5).intersects(5..6) shouldBe true
        (1..5).intersect(-3..2) rangeShouldBe 1..2
        (1..5).intersect(4..6) rangeShouldBe 4..5
        (1..5).intersect(2..4) rangeShouldBe 2..4
        (1..5).intersect(-3..6) rangeShouldBe 1..5
        (1..5).intersect(-5..-3) shouldBe null
        (1..5).intersect(6..8) shouldBe null
        (1..5).intersect(1..5) rangeShouldBe 1..5
        (1..5).intersect(5..6) rangeShouldBe 5..5
    }

    "test intersects bounds excluded" {
        (1..5).intersectsBoundsExcluded(5..6) shouldBe false
        (1..5).intersectsBoundsExcluded(4..6) shouldBe true
        (-1..5).intersectsBoundsExcluded(-5..-1) shouldBe false
        (-1..5).intersectsBoundsExcluded(-5..5) shouldBe true
    }

    /**
     * Asserts that the collection contains exactly the [expected] elements. Instead of using
     * equality check between ranges (i.e. == operator), [coincidesWith] is used.
     */
    fun <T : Comparable<T>> List<ClosedRange<T>>.shouldContainRanges(vararg expected: ClosedRange<T>) {
        expected.toMutableList().let { expectedRanges ->
            this.forEach { actualRange ->
                expectedRanges.filter { it.coincidesWith(actualRange) }.let { equalRanges ->
                    assertTrue(equalRanges.size == 1)
                    expectedRanges.removeAll(equalRanges)
                }
            }
            assertTrue(expectedRanges.isEmpty())
        }
    }

    "test range subtraction" {
        ((1..5) - (-3..2)).shouldContainRanges(2..5)
        ((1..5) - (4..6)).shouldContainRanges(1..4)
        ((1..5) - (2..4)).shouldContainRanges(1..2, 4..5)
        assertTrue(((1..5) - (-3..6)).isEmpty())
        ((1..5) - (-5..-3)).shouldContainRanges(1..5)
        ((1..5) - (6..8)).shouldContainRanges(1..5)
        ((1..5) - (5..6)).shouldContainRanges(1..5)
        (1..5).subtractAll(listOf(-3..2, 4..6)).shouldContainRanges(2..4)
        assertTrue((1..5).subtractAll(listOf(-3..2, 4..6, 2..4)).isEmpty())
        (1..5).subtractAll(listOf(2..4)).shouldContainRanges(1..2, 4..5)
    }

    /* old tests below */

    fun assertIntersectionIsNull(first: Double, second: Double, third: Double, fourth: Double) =
        assertNull(rangeFromUnordered(first, second).intersect(rangeFromUnordered(third, fourth)))

    fun assertIntersectionIs(
        first: Double,
        second: Double,
        third: Double,
        fourth: Double,
        intersectionFirst: Double,
        intersectionSecond: Double,
    ) {
        val intersection = rangeFromUnordered(first, second).intersect(rangeFromUnordered(third, fourth))
        Assertions.assertNotNull(intersection)
        intersection?.let {
            it.start shouldBe intersectionFirst
            it.endInclusive shouldBe intersectionSecond
        }
    }

    "test intersection 2" {
        assertIntersectionIs(-1.0, 5.0, 2.0, 3.0, 2.0, 3.0)
        assertIntersectionIs(-1.0, 5.0, -2.0, 6.0, -1.0, 5.0)
        assertIntersectionIs(-1.0, 5.0, -2.0, 3.0, -1.0, 3.0)
        assertIntersectionIsNull(-1.0, 5.0, -3.0, -2.0)
        assertIntersectionIs(-1.0, 5.0, 3.0, 6.0, 3.0, 5.0)
        assertIntersectionIsNull(-1.0, 5.0, 6.0, 7.0)
    }

    fun assertSubtractionIs(
        first: Double,
        second: Double,
        third: Double,
        fourth: Double,
        vararg expected: Double,
    ) {
        (rangeFromUnordered(first, second) - rangeFromUnordered(third, fourth))
            .flatMap { mutableListOf(it.start, it.endInclusive) }
            .toDoubleArray() shouldBe expected
    }

    "test subtraction 2" {
        assertSubtractionIs(-1.0, 5.0, 2.0, 3.0, -1.0, 2.0, 3.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -2.0, 6.0)
        assertSubtractionIs(-1.0, 5.0, -2.0, 3.0, 3.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -3.0, -2.0, -1.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, 3.0, 6.0, -1.0, 3.0)
        assertSubtractionIs(-1.0, 5.0, 6.0, 7.0, -1.0, 5.0)
        assertSubtractionIs(-1.0, 5.0, -1.0, 3.0, 3.0, 5.0)
    }
})
