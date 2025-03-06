/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.times.DoubleTime

/**
 * A trigger that fires at a given [time].
 */
class Trigger<T>(time: Time) : AbstractDistribution<T>(time) {
    override fun getRate(): Double = Double.Companion.NaN

    override fun updateStatus(currentTime: Time?, executed: Boolean, param: Double, environment: Environment<T, *>) {
        if (executed) {
            setNextOccurrence(DoubleTime(Double.Companion.POSITIVE_INFINITY))
        }
    }

    override fun cloneOnNewNode(destination: Node<T>, currentTime: Time): AbstractDistribution<T> =
        Trigger(nextOccurence)

    /**
     * Static constants for [Trigger] class.
     */
    companion object {
        private const val serialVersionUID = 5207992119302525618L
    }
}
