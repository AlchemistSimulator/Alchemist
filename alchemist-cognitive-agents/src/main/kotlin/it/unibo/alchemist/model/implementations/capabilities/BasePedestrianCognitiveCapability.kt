/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianCognitiveCapability

/**
 * The node's [CognitiveModel].
 */
data class BasePedestrianCognitiveCapability<T>(
    override val node: Node<T>,
    override val cognitiveModel: CognitiveModel,
) : PedestrianCognitiveCapability<T>
