package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * Move the node towards positions of the environment with a high concentration of the target molecule.
 *
 * @param euclidean
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param node
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

    private val followScalarField = getLayerOrFail().let { layer ->
        CognitiveAgentFollowScalarField(environment, reaction, pedestrian, layer.center()) {
            layer.concentrationIn(it)
        }
    }

    override fun nextPosition(): Euclidean2DPosition = followScalarField.nextPosition()

    override fun cloneAction(node: Node<Number>, reaction: Reaction<Number>): CognitiveAgentFollowLayer =
        CognitiveAgentFollowLayer(environment, reaction, node.pedestrianProperty, targetMolecule)
}
