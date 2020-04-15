package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment

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
    env: Physics2DEnvironment<Number>,
    reaction: Reaction<Number>,
    pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule
) : FlowFieldSteeringAction(env, reaction, pedestrian, targetMolecule) {

    override fun cloneAction(n: Node<Number>, r: Reaction<Number>): Action<Number> =
        FollowFlowField(env, r, n as Pedestrian2D<Number>, targetMolecule)

    override fun List<Euclidean2DPosition>.selectPosition(
        layer: Layer<Number, Euclidean2DPosition>,
        currentConcentration: Double
    ): Euclidean2DPosition = toMutableList()
        .apply {
            if (layer is BidimensionalGaussianLayer<*>) {
                /*
                 * If the layer is Gaussian (i.e. has a center), probably the most suitable
                 * position is the one obtained by moving away from the center along the
                 * direction which connects the current position to the center.
                 */
                val center = env.makePosition(layer.centerX, layer.centerY)
                this.add(currentPosition + (center - currentPosition).resize(maxWalk()))
            }
        }
        .discardUnsuitablePositions()
        .map { it to layer.concentrationIn(it) }
        .filter { it.second > currentConcentration }
        .maxBy { it.second }?.first ?: currentPosition
}
