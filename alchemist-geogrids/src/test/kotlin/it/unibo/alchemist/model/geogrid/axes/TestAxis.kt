/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.axes

import it.unibo.alchemist.model.geogrid.Axis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestAxis {

    // indices 0..3
    private val axis = ImmutableAxis(doubleArrayOf(10.0, 20.0, 30.0, 40.0))
    private val singleNode = ImmutableAxis(doubleArrayOf(42.0))

    @Test
    fun `a coordinate on a value has a degenerate cell`() {
        assertEquals(Axis.Cell(2, 2, 0.0), axis.enclosingCellOf(30.0))
    }

    @Test
    fun `an interior coordinate is weighted by its position in the cell`() {
        // sets a tolerance for the results
        val tolerance = 1e-9
        val cell = axis.enclosingCellOf(23.0)
        assertEquals(1, cell.lower)
        assertEquals(2, cell.upper)
        assertEquals(0.3, cell.weight, tolerance)
        assertEquals(0.5, axis.enclosingCellOf(25.0).weight, tolerance)
    }

    @Test
    fun `coordinates outside the axis clamp to its ends`() {
        assertEquals(Axis.Cell(0, 0, 0.0), axis.enclosingCellOf(5.0))
        assertEquals(Axis.Cell(3, 3, 0.0), axis.enclosingCellOf(50.0))
    }

    @Test
    fun `a single-value axis always has a degenerate cell`() {
        assertEquals(Axis.Cell(0, 0, 0.0), singleNode.enclosingCellOf(42.0))
        assertEquals(Axis.Cell(0, 0, 0.0), singleNode.enclosingCellOf(0.0))
        assertEquals(Axis.Cell(0, 0, 0.0), singleNode.enclosingCellOf(100.0))
    }

    @Test
    fun `a NaN query is rejected`() {
        assertFailsWith<IllegalArgumentException> { axis.enclosingCellOf(Double.NaN) }
    }

    @Test
    fun `an empty axis is rejected`() {
        assertFailsWith<IllegalArgumentException> { ImmutableAxis(DoubleArray(0)) }
    }

    @Test
    fun `coordinates that are not strictly ascending are rejected`() {
        assertFailsWith<IllegalArgumentException> { ImmutableAxis(doubleArrayOf(30.0, 20.0, 10.0)) }
        assertFailsWith<IllegalArgumentException> { ImmutableAxis(doubleArrayOf(10.0, 20.0, 20.0)) }
    }

    @Test
    fun `NaN coordinates are rejected`() {
        assertFailsWith<IllegalArgumentException> { ImmutableAxis(doubleArrayOf(10.0, Double.NaN, 30.0)) }
        assertFailsWith<IllegalArgumentException> { ImmutableAxis(doubleArrayOf(Double.NaN)) }
    }

    @Test
    fun `changing the source array does not change the axis`() {
        val source = doubleArrayOf(10.0, 20.0, 30.0)
        val axis = ImmutableAxis(source)
        source[0] = 100.0
        assertEquals(10.0, axis[0])
    }

    @Test
    fun `changing the exported array does not change the axis`() {
        axis.toDoubleArray()[0] = 100.0
        assertEquals(10.0, axis[0])
    }

    @Test
    fun `the range goes from the first to the last coordinate`() {
        assertEquals(10.0..40.0, axis.range)
    }

    @Test
    fun `cells with invalid indices or weights are rejected`() {
        listOf(
            { Axis.Cell(-1, 0, 0.0) },
            { Axis.Cell(1, 0, 0.0) },
            { Axis.Cell(0, 2, 0.5) },
            { Axis.Cell(0, 1, 1.5) },
            { Axis.Cell(0, 1, Double.NaN) },
            { Axis.Cell(1, 1, 0.5) },
        ).forEach { cell -> assertFailsWith<IllegalArgumentException> { cell() } }
    }
}
