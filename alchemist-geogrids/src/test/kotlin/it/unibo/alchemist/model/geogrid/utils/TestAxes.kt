/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import it.unibo.alchemist.model.geogrid.utils.AxisCell.Companion.enclosingCellOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestAxes {

    // indices 0..3
    private val axis = doubleArrayOf(10.0, 20.0, 30.0, 40.0)
    private val singleNode = doubleArrayOf(42.0)

    @Test
    fun `a coordinate on a value has a degenerate cell`() {
        assertEquals(AxisCell(2, 2, 0.0), axis.enclosingCellOf(30.0))
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
        assertEquals(AxisCell(0, 0, 0.0), axis.enclosingCellOf(5.0))
        assertEquals(AxisCell(3, 3, 0.0), axis.enclosingCellOf(50.0))
    }

    @Test
    fun `a single-value axis always has a degenerate cell`() {
        assertEquals(AxisCell(0, 0, 0.0), singleNode.enclosingCellOf(42.0))
        assertEquals(AxisCell(0, 0, 0.0), singleNode.enclosingCellOf(0.0))
        assertEquals(AxisCell(0, 0, 0.0), singleNode.enclosingCellOf(100.0))
    }

    @Test
    fun `an empty axis and a NaN query are rejected`() {
        assertFailsWith<IllegalArgumentException> { DoubleArray(0).enclosingCellOf(5.0) }
        assertFailsWith<IllegalArgumentException> { axis.enclosingCellOf(Double.NaN) }
    }
}
