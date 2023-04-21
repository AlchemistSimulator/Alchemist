/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.cognitiveagents.CognitiveModel

/**
 * The pedestrian's cognitive capability.
 */
interface CognitiveProperty<T> : NodeProperty<T> {
    /**
     * The molecule associated with danger in the environment.
     */
    val danger: Molecule?

    /**
     * The pedestrian's cognitive model.
     */
    val cognitiveModel: CognitiveModel

    /**
     * The mind model of all people considered influential for this cognitive pedestrian.
     */
    fun influentialPeople(): List<CognitiveModel> = node.asProperty<T, PerceptiveProperty<T>>()
        .senses.flatMap { it.value.influentialNodes() }
        .mapNotNull { it.asPropertyOrNull<T, CognitiveProperty<T>>() }
        .map { it.cognitiveModel }
}
