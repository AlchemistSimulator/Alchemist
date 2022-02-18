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
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianCognitiveCapability
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * The node's [CognitiveModel].
 */
data class BasePedestrianCognitiveCapability<T, P, A, F> @JvmOverloads constructor(
    /**
     * The environment in which the node moves.
     */
    val environment: PhysicsEnvironment<T, P, A, F>,
    override val node: Node<T>,
    /**
     * The molecule associated with danger in the environment.
     */
    val danger: Molecule? = null,
    val cognitiveModelCreator: () -> CognitiveModel,
) : PedestrianCognitiveCapability<T>
where P : Position<P>,
      P : Vector<P>,
      A : GeometricTransformation<P>,
      F : GeometricShapeFactory<P, A> {
    override val cognitiveModel: CognitiveModel by lazy(cognitiveModelCreator)
}
