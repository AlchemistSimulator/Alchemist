/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.times.DoubleTime

/**
 * Generates a constant delay between occurrences.
 *
 * @param start initial scheduling time
 * @param rate number of occurrences per time unit
 */
open class DiracComb<T>(start: Time, rate: Double) : AbstractDistribution<T>(start) {

    private val timeInterval = 1 / rate

    /**
     * @param rate number of occurrences per time unit
     */
    constructor(rate: Double) : this(Time.ZERO, rate)

    /** Number of occurrences per time unit represented by this constant-delay law. */
    val frequency: Double get() = 1 / timeInterval

    override fun sample(): Time = DoubleTime(timeInterval)

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = DiracComb(startTime, frequency)

    override fun toString(): String = "${javaClass.simpleName}[ΔT=$timeInterval]"
}
