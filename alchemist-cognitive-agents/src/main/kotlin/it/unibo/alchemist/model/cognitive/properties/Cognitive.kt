/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.properties

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.PhysicsEnvironment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.cognitive.CognitiveModel
import it.unibo.alchemist.model.cognitive.CognitiveProperty
import it.unibo.alchemist.model.cognitive.HumanProperty
import it.unibo.alchemist.model.cognitive.impact.ImpactModel
import it.unibo.alchemist.model.euclidean.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.properties.AbstractNodeProperty

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
          A : Transformation<P>,
          F : GeometricShapeFactory<P, A> {
    override val cognitiveModel: CognitiveModel by lazy {
        ImpactModel(
            node.asProperty<T, HumanProperty<T, P, A>>().compliance,
            ::influentialPeople,
        ) {
            environment.getLayer(danger)
                .map { it.getValue(environment.getPosition(node)) as Double }
                .orElse(0.0)
        }
    }

    override fun cloneOnNewNode(node: Node<T>) = Cognitive(environment, node, danger)

    override fun toString() = "${super.toString()}[dangerMolecule=$danger]"
}
