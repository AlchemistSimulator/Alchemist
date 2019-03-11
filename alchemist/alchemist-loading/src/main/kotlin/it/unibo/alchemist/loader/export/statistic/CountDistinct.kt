/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.statistic

import org.apache.commons.math3.stat.descriptive.AbstractUnivariateStatistic

/**
 * Counts the number of distinct entries
 */
class CountDistinct : AbstractUnivariateStatistic() {

    override fun evaluate(values: DoubleArray, begin: Int, length: Int) = values
        .asSequence()
        .drop(begin)
        .take(length)
        .distinct()
        .count().toDouble()

    override fun copy() = this

    override fun toString() = javaClass.simpleName
}