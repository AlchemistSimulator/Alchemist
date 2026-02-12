/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

/**
 * Theoretical model that describes the cognitive processes of an agent.
 *
 * Implementations provide measures for the agent's belief about danger, fear level,
 * and competing intentions to escape or remain.
 */
interface CognitiveModel {
    /**
     * Returns the agent's current belief about the situation's dangerousness.
     *
     * @return the perceived level of dangerousness as a [Double].
     */
    fun dangerBelief(): Double

    /**
     * Returns the agent's current fear level.
     *
     * @return the fear level as a [Double].
     */
    fun fear(): Double

    /**
     * Returns the agent's intention to escape. This value is opposed to [remainIntention].
     *
     * @return the intention to escape as a [Double].
     */
    fun escapeIntention(): Double

    /**
     * Returns the agent's intention to remain. This value is opposed to [escapeIntention].
     *
     * @return the intention to remain as a [Double].
     */
    fun remainIntention(): Double

    /**
     * Update the model internal state using the provided update frequency.
     *
     * @param frequency the update frequency (time between updates or update rate), represented as a [Double].
     */
    fun update(frequency: Double)

    /**
     * Whether the agent currently intends to escape.
     *
     * @return true if the escape intention is greater than the remain intention; false otherwise.
     */
    fun wantsToEscape(): Boolean = escapeIntention() > remainIntention()
}
