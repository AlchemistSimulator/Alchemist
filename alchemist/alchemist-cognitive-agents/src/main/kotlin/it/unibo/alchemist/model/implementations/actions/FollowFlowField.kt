package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian2D
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
open class FollowFlowField(
    env: Euclidean2DEnvironment<Number>,
    reaction: Reaction<Number>,
    pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule
) : AbstractFlowFieldAction(env, reaction, pedestrian, targetMolecule) {

    override fun cloneAction(n: Node<Number>, r: Reaction<Number>): FollowFlowField =
        FollowFlowField(environment, r, n as Pedestrian2D<Number>, targetMolecule)

    override fun Sequence<Euclidean2DPosition>.selectPosition(
        layer: Layer<Number, Euclidean2DPosition>,
        currentConcentration: Double
    ): Euclidean2DPosition = this
        .let {
            layer.center()?.let { center ->
                /*
                 * If the layer has a center, probably the most suitable position is the one obtained by moving towards
                 * the center along the direction which connects the current position to the center.
                 */
                it + (currentPosition + (center - currentPosition).resized(maxWalk))
            } ?: it
        }
        .discardUnsuitablePositions(environment, pedestrian)
        .map { it to layer.concentrationIn(it) }
        .filter { it.second > currentConcentration }
        .maxBy { it.second }?.first ?: currentPosition
}
