/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.jakta.timedistributions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

data class JaktaTimeDistribution<T>(
    val sense: TimeDistribution<T>,
    val deliberate: TimeDistribution<T>,
    val act: TimeDistribution<T>,
) : TimeDistribution<T> {
    /**
     * Unknown, as the three phases follow distinct distributions and JaKtA owns their scheduling.
     * The mean delay of a whole sense-deliberate-act cycle is the sum of the mean delays of the phases,
     * but the mean delay between occurrences depends on how many occurrences a cycle takes and on whether
     * any phase is skipped. Moreover, this distribution cannot be sampled at all.
     */
    override val meanDelay: Double = Double.NaN

    override fun newInstanceOn(node: Node<T>): TimeDistribution<T> = JaktaTimeDistribution(
        sense.newInstanceOn(node),
        deliberate.newInstanceOn(node),
        act.newInstanceOn(node),
    )

    override fun sample(): Time = doNotUse()

    override fun toString(): String = "JaktaTimeDistribution(sense=$sense, deliberate=$deliberate, act=$act)"

    private fun doNotUse(): Nothing = error(
        "${this::class.simpleName} is not meant to be used directly, but to host custom time distributions" +
            " for the sense, deliberate, and act phases of the JaKtA lifecycle.",
    )
}
