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