/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * Generates non-negative delays between occurrences.
 *
 * The sampling operation follows the same separation used by Apache Commons Math distributions: drawing a value
 * does not know which reaction consumes it or how that reaction is scheduled. Reactions convert sampled delays to
 * absolute simulation times and own every decision about preserving, replacing, or transforming an occurrence.
 */
interface TimeDistribution<T> {

    /**
     * Draws the next delay.
     *
     * Implementations must return a non-negative, finite [Time]. Invalid samples are rejected at the reaction
     * boundary so custom implementations receive the same validation as built-in distributions.
     *
     * @return a newly sampled delay
     */
    fun sample(): Time

    /**
     * Creates a fresh generator with the same configuration for [node].
     *
     * The returned instance may share simulation services such as a random generator, but it must not inherit
     * samples or other generator-local runtime state. Scheduling at the destination is owned by the reaction.
     *
     * @param node destination node
     * @return a fresh destination-bound generator
     */
    fun newInstanceOn(node: Node<T>): TimeDistribution<T>
}
