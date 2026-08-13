/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.timedistributions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.sapere.dsl.IExpression
import it.unibo.alchemist.model.sapere.dsl.ITreeNode
import it.unibo.alchemist.model.sapere.dsl.impl.Expression
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.times.DoubleTime
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.Semaphore
import org.apache.commons.math3.random.RandomGenerator
import org.danilopianini.lang.HashString

/** An exponential generator whose rate is computed from a SAPERE match expression. */
@JvmSuppressWildcards
class SAPEREExponentialTime(private val rateEquation: String, start: Time, randomGenerator: RandomGenerator) :
    ExponentialTime<@JvmSuppressWildcards List<ILsaMolecule>>(Double.NaN, start, randomGenerator),
    SAPERETimeDistribution {

    /** Builds a generator starting at time zero. */
    constructor(rateEquation: String, randomGenerator: RandomGenerator) :
        this(rateEquation, DoubleTime(), randomGenerator)

    private val staticRate = rateEquation.toDoubleOrNull() ?: 0.0
    private val numericRate = rateEquation.toDoubleOrNull() != null
    private val expression: IExpression = if (numericRate) {
        if (staticRate.isInfinite()) {
            Expression("asap")
        } else {
            FORMAT_MUTEX.acquireUninterruptibly()
            try {
                Expression(FORMAT.format(staticRate))
            } finally {
                FORMAT_MUTEX.release()
            }
        }
    } else {
        Expression(rateEquation)
    }
    private var matches: Map<HashString, ITreeNode<*>>? = null

    override fun getRate(): Double = if (numericRate) {
        staticRate
    } else {
        expression.calculate(matches).getValue(matches) as Double
    }

    override val lambda: Double get() = getRate()

    override fun sample(): Time = genTime(getRate())

    override fun newInstanceOn(node: Node<List<ILsaMolecule>>): TimeDistribution<List<ILsaMolecule>> =
        SAPEREExponentialTime(rateEquation, startTime, randomGenerator)

    override fun isStatic(): Boolean = numericRate

    override fun setMatches(match: Map<HashString, ITreeNode<*>>) {
        if (!numericRate) {
            matches = match
        }
    }

    override fun getRateEquation(): IExpression = expression

    private companion object {
        private const val FORMAT_PATTERN = "###.######################"
        private val FORMAT = DecimalFormat(FORMAT_PATTERN, DecimalFormatSymbols.getInstance(Locale.ENGLISH))
        private val FORMAT_MUTEX = Semaphore(1)
    }
}
