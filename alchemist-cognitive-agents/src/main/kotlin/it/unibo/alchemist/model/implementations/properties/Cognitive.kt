/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.cognitiveagents.CognitiveModel
import it.unibo.alchemist.model.cognitiveagents.impact.ImpactModel
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.model.interfaces.properties.HumanProperty
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironment
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * The node's [CognitiveModel].
 */
data class Cognitive<T, P, A, F> @JvmOverloads constructor(
    private val environment: PhysicsEnvironment<T, P, A, F>,
    override val node: Node<T>,
    override val danger: Molecule? = null,
) : AbstractNodeProperty<T>(node), CognitiveProperty<T>
where P : Position<P>,
      P : Vector<P>,
      A : GeometricTransformation<P>,
      F : GeometricShapeFactory<P, A> {
    override val cognitiveModel: CognitiveModel by lazy {
        ImpactModel(
            node.asProperty<T, HumanProperty<T, P, A>>().compliance, ::influentialPeople,
        ) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(node)) as Double }
                .orElse(0.0)
        }
    }

    override fun cloneOnNewNode(node: Node<T>) = Cognitive(environment, node, danger)

    override fun toString() = "${super.toString()}[dangerMolecule=$danger]"
}
