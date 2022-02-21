/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.capabilities

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapabilityOrNull

/**
 * The pedestrian's cognitive capability.
 */
interface PedestrianCognitiveCapability<T> : Capability<T> {
    /**
     * The pedestrian's cognitive model.
     */
    val cognitiveModel: CognitiveModel

    /**
     * The mind model of all people considered influential for this cognitive pedestrian.
     */
    fun influentialPeople(): List<CognitiveModel> = node.asCapability<T, PerceptionOfOthers<T>>()
        .senses.flatMap { it.value.influentialNodes() }
        .mapNotNull { it.asCapabilityOrNull<T, PedestrianCognitiveCapability<T>>() }
        .map { it.cognitiveModel }
}
