package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment

/**
 * Move the pedestrian towards positions of the environment with a high concentration of the target molecule.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 */
open class CognitiveAgentFollowLayer(
    env: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    pedestrian: Node<Number>,
    targetMolecule: Molecule
) : AbstractLayerAction(env, reaction, pedestrian, targetMolecule) {

    private val followScalarField = getLayerOrFail().let { layer ->
        CognitiveAgentFollowScalarField(environment, reaction, pedestrian, layer.center()) {
            layer.concentrationIn(it)
        }
    }

    override fun nextPosition(): Euclidean2DPosition = followScalarField.nextPosition()

    override fun cloneAction(node: Node<Number>, reaction: Reaction<Number>): CognitiveAgentFollowLayer =
        CognitiveAgentFollowLayer(environment, reaction, node, targetMolecule)
}
