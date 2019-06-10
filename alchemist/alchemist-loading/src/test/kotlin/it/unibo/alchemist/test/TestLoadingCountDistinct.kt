/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.loader.export.StatUtil
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestLoadingCountDistinct {

    /**
     * Tests loading and executing CountDistinct
     */
    @Test
    fun `test loading "CountDistinct" and its execution`() {
        val stat = StatUtil.makeUnivariateStatistic("countdistinct").get()
        Assertions.assertEquals(3.0, stat.evaluate(doubleArrayOf(1.0, 2.0, 3.0, 1.0)), Double.MIN_VALUE)
        Assertions.assertEquals(100.0, stat.evaluate((1..100).map(Int::toDouble).toDoubleArray()), 100.0)
        Assertions.assertEquals(100.0, stat.evaluate((1..100).map(Int::toDouble).toDoubleArray(), 50, 1000), 50.0)
    }
}