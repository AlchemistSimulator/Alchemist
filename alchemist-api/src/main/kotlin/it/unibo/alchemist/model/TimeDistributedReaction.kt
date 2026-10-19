/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import it.unibo.alchemist.model.util.asFinitePositiveMeanDelayOrThrow

/**
 * A [Reaction] whose putative occurrence delays are sampled from a [timeDistribution].
 *
 * Most time-distributed reactions recur. A single-use implementation may instead remove itself after its first
 * successful execution. Both expose their sampler and the [meanDelay] between their occurrences.
 */
interface TimeDistributedReaction<T> : Reaction<T> {

    /**
     * The current [meanDelay], for consumers requiring it to be finite and positive, such as those converting speeds
     * into per-occurrence movements.
     *
     * @throws IllegalStateException if the [meanDelay] is not finite and positive
     */
    val finitePositiveMeanDelayOrError: Double get() = meanDelay.asFinitePositiveMeanDelayOrThrow(this)

    /**
     * The expected delay between two occurrences of this reaction in the current model state.
     *
     * It may differ from the [TimeDistribution.meanDelay] of [timeDistribution], for instance when the state of the
     * model scales the sampled delays. It is [Double.POSITIVE_INFINITY] if the reaction is not expected to occur again,
     * and [Double.NaN] if it is unknown. It may change along with the model state: consumers should read it when they
     * need it rather than storing it.
     */
    val meanDelay: Double

    /** The delay distribution governing this reaction's putative occurrences. */
    val timeDistribution: TimeDistribution<T>

    /**
     * Advances distribution-backed scheduling after successful execution at [currentTime].
     *
     * Normal scheduled execution invokes this through [Reaction.execute].
     * A single-use reaction removes itself during execution, so their execution never reaches this operation.
     */
    fun updateSchedulingAfterFiring(currentTime: Time)
}
