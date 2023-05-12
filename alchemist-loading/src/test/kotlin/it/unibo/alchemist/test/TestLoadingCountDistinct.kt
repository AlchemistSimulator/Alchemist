/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.shouldBeExactly
import it.unibo.alchemist.util.StatUtil

/**
 * Tests loading and executing CountDistinct.
 */
class TestLoadingCountDistinct : StringSpec({
    "CountDistinct should load and execute" {
        val stat = StatUtil.makeUnivariateStatistic("countdistinct").get()
        stat.evaluate(doubleArrayOf(1.0, 2.0, 3.0, 1.0)) shouldBeExactly 3.0
        stat.evaluate((1..100).map(Int::toDouble).toDoubleArray()) shouldBeExactly 100.0
        stat.evaluate((1..100).map(Int::toDouble).toDoubleArray(), 50, 1000) shouldBeExactly 50.0
    }
})
