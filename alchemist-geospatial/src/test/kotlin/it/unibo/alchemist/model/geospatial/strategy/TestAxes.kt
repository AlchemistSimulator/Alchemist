/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class TestAxes : StringSpec({

    // indices 0..3
    val axis = doubleArrayOf(10.0, 20.0, 30.0, 40.0)
    val singleNode = doubleArrayOf(42.0)

    // nearestIndex tests
    "nearestIndex returns the exact index on a node hit" {
        nearestIndex(axis, 10.0) shouldBe 0
        nearestIndex(axis, 20.0) shouldBe 1
        nearestIndex(axis, 40.0) shouldBe 3
    }

    "nearestIndex picks the closer node between two nodes" {
        // closer to 20
        nearestIndex(axis, 22.0) shouldBe 1
        // closer to 30
        nearestIndex(axis, 28.0) shouldBe 2
    }

    "nearestIndex resolves an exact tie to the lower index" {
        nearestIndex(axis, 25.0) shouldBe 1
    }

    "nearestIndex clamps below the first node" {
        nearestIndex(axis, 5.0) shouldBe 0
        nearestIndex(axis, -1000.0) shouldBe 0
    }

    "nearestIndex clamps above the last node" {
        nearestIndex(axis, 50.0) shouldBe 3
        nearestIndex(axis, 1000.0) shouldBe 3
    }

    "nearestIndex always returns 0 on a single-node axis" {
        nearestIndex(singleNode, 42.0) shouldBe 0
        nearestIndex(singleNode, 0.0) shouldBe 0
        nearestIndex(singleNode, 100.0) shouldBe 0
    }

    "nearestIndex rejects an empty axis" {
        shouldThrow<IllegalArgumentException> { nearestIndex(DoubleArray(0), 5.0) }
    }

    "nearestIndex rejects a NaN query" {
        shouldThrow<IllegalArgumentException> { nearestIndex(axis, Double.NaN) }
    }

    // bracketIndices tests
    "bracketIndices returns a degenerate pair on a node hit" {
        bracketIndices(axis, 30.0) shouldBe (2 to 2)
    }

    "bracketIndices brackets an interior coordinate" {
        bracketIndices(axis, 23.0) shouldBe (1 to 2)
    }

    "bracketIndices clamps outside the boundaries" {
        bracketIndices(axis, 5.0) shouldBe (0 to 0)
        bracketIndices(axis, 50.0) shouldBe (3 to 3)
    }

    "bracketIndices is degenerate on a single-node axis" {
        bracketIndices(singleNode, 42.0) shouldBe (0 to 0)
        bracketIndices(singleNode, 0.0) shouldBe (0 to 0)
        bracketIndices(singleNode, 100.0) shouldBe (0 to 0)
    }

    "bracketIndices rejects an empty axis and a NaN query" {
        shouldThrow<IllegalArgumentException> { bracketIndices(DoubleArray(0), 5.0) }
        shouldThrow<IllegalArgumentException> { bracketIndices(axis, Double.NaN) }
    }

    // weight tests
    "weight is 0.0 at the lower node and 1.0 at the upper node" {
        weight(axis, 1, 2, 20.0) shouldBe 0.0
        weight(axis, 1, 2, 30.0) shouldBe 1.0
    }

    "weight is the normalized position within the segment" {
        // sets a tolerance for the results
        val tolerance = 1e-9
        weight(axis, 1, 2, 25.0) shouldBe (0.5 plusOrMinus tolerance)
        weight(axis, 1, 2, 23.0) shouldBe (0.3 plusOrMinus tolerance)
    }

    "weight is 0.0 on a degenerate bracket" {
        weight(axis, 2, 2, 30.0) shouldBe 0.0
    }
})
