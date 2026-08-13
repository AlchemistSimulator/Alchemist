/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.times.DoubleTime
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.util.FastMath

/**
 * Generates exponentially distributed delays.
 *
 * @param lambda configured exponential rate
 * @param start initial scheduling time
 * @param randomGenerator simulation random generator
 */
@SuppressFBWarnings(value = ["EI_EXPOSE_REP2"], justification = "The simulation intentionally shares its RNG")
open class ExponentialTime<T>(open val lambda: Double, start: Time, protected val randomGenerator: RandomGenerator) :
    AbstractDistribution<T>(start) {

    /**
     * @param rate configured exponential rate
     * @param randomGenerator simulation random generator
     */
    constructor(rate: Double, randomGenerator: RandomGenerator) : this(rate, Time.ZERO, randomGenerator)

    /**
     * Generates an exponential delay at [propensity].
     *
     * This protected operation supports specialized generators whose distribution parameter is computed from a
     * domain token. Scheduling transformations remain reaction-owned.
     */
    protected open fun genTime(propensity: Double): Time = DoubleTime(uniformToExponential(propensity))

    override fun sample(): Time = genTime(lambda)

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = ExponentialTime(lambda, startTime, randomGenerator)

    private fun uniformToExponential(lambda: Double): Double = -FastMath.log1p(-randomGenerator.nextDouble()) / lambda
}
