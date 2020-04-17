package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Position2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.Vector2D

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
open class FollowFlowField<P>(
    env: Environment<Number, P>,
    reaction: Reaction<Number>,
    pedestrian: Pedestrian2D<Number>,
    targetMolecule: Molecule
) : FlowFieldSteeringAction<P>(env, reaction, pedestrian, targetMolecule)
    where
        P : Position2D<P>,
        P : Vector2D<P> {

    override fun cloneAction(n: Node<Number>, r: Reaction<Number>): Action<Number> =
        FollowFlowField(env, r, n as Pedestrian2D<Number>, targetMolecule)

    override fun List<P>.selectPosition(layer: Layer<Number, P>, currentConcentration: Double): P = this
        .toMutableList()
        .apply {
            if (layer is BidimensionalGaussianLayer<*>) {
                /*
                 * If the layer is Gaussian (i.e. has a center), probably the most suitable
                 * position is the one obtained by moving away from the center along the
                 * direction which connects the current position to the center.
                 */
                val center = env.makePosition(layer.centerX, layer.centerY)
                this.add(currentPosition + (center - currentPosition).resized(maxWalk()))
            }
        }
        .discardUnsuitablePositions(env, pedestrian)
        .map { it to layer.concentrationIn(it) }
        .filter { it.second > currentConcentration }
        .maxBy { it.second }?.first ?: currentPosition
}
