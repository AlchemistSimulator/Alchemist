/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitiveagents

/**
 * Theoretical model to describe the cognitive processes underlying in an agent.
 */
interface CognitiveModel {

    /**
     * Value representing the current belief of the situation dangerousness.
     */
    fun dangerBelief(): Double

    /**
     * Value representing the level of fear.
     */
    fun fear(): Double

    /**
     * Value representing the intention to escape. Opposed to [remainIntention].
     */
    fun escapeIntention(): Double

    /**
     * Value representing the intention to remain. Opposed to [escapeIntention]
     */
    fun remainIntention(): Double

    /**
     * Update the current intensity of the aforementioned feelings considering a [frequency].
     */
    fun update(frequency: Double)
}
