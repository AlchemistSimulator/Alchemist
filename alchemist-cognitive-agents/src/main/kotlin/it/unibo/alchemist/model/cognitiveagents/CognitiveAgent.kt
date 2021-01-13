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
 * An entity capable of having emotions and relations.
 */
interface CognitiveAgent {

    /**
     * The cognitive model this agent adheres to.
     */
    val cognitive: CognitiveModel

    /**
     * Whether or not this pedestrian intends to escape.
     */
    fun wantsToEscape(): Boolean =
        cognitive.escapeIntention() > cognitive.remainIntention()

    /**
     * A list of all the cognitive agents who exert an influence on this cognitive agent.
     */
    fun influencialPeople(): List<CognitiveAgent>
}
