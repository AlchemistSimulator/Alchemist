/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.loader.export.StatUtil
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic
import org.junit.Assert
import org.junit.Test
import java.util.Optional

class TestLoadingCountDistinct {

    /**
     * Tests loading and executing CountDistinct
     */
    @Test
    fun `test loading "CountDistinct" and its execution`() {
        val stat = StatUtil.makeUnivariateStatistic("countdistinct")?.let(Optional<UnivariateStatistic>::get)
        Assert.assertNotNull(stat)
        Assert.assertEquals(3.0, stat!!.evaluate(doubleArrayOf(1.0, 2.0, 3.0, 1.0)), 0.0)
        Assert.assertEquals(100.0, stat!!.evaluate((1..100).map(Int::toDouble).toDoubleArray()), 100.0)
        Assert.assertEquals(100.0, stat!!.evaluate((1..100).map(Int::toDouble).toDoubleArray(), 50, 1000), 50.0)
    }
}