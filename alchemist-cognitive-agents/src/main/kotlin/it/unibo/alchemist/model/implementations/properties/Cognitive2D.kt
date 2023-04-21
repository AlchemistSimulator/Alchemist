/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty

/**
 * The node's [CognitiveModel].
 */
class Cognitive2D<T> @JvmOverloads constructor(
    /**
     * The environment in which the node moves.
     */
    environment: Physics2DEnvironment<T>,
    node: Node<T>,
    /**
     * The molecule associated with danger in the environment.
     */
    danger: Molecule? = null,
) : CognitiveProperty<T> by Cognitive(environment, node, danger)
