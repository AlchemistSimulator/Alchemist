/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Move the node towards positions of the environment with a high concentration of the target molecule.
 *
 * @param euclidean
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 */
open class CognitiveAgentFollowLayer(
    euclidean: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    override val pedestrian: PedestrianProperty<Number>,
    targetMolecule: Molecule,
) : AbstractLayerAction(euclidean, reaction, pedestrian, targetMolecule) {
    private val followScalarField =
        getLayerOrFail().let { layer ->
            CognitiveAgentFollowScalarField(environment, reaction, pedestrian, layer.center()) {
                layer.concentrationIn(it)
            }
        }

    override fun nextPosition(): Euclidean2DPosition = followScalarField.nextPosition()

    override fun cloneAction(node: Node<Number>, reaction: Reaction<Number>): CognitiveAgentFollowLayer =
        CognitiveAgentFollowLayer(
            environment,
            reaction,
            node.pedestrianProperty,
            targetMolecule,
        )
}
