package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.implementations.utils.origin
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Move the pedestrian towards positions of the environment with a high concentration of the target molecule.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
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
) : FlowFieldSteeringAction<Number>(
    env,
    reaction,
    pedestrian,
    targetMolecule,
    { molecule ->
        val currentPosition = env.getPosition(pedestrian)
        val layer = env.getLayer(molecule).get()
        val currentConcentration = layer.getValue(currentPosition).toDouble()
        this.map { it to layer.getValue(it).toDouble() }
            .filter { it.second > currentConcentration }
            .maxBy { it.second }?.first ?: currentPosition
    },
    TargetSelectionStrategy {
        with(env.getLayer(targetMolecule)) {
            if (isEmpty || get() !is BidimensionalGaussianLayer<*>) {
                env.origin()
            } else {
                with(get() as BidimensionalGaussianLayer) {
                    env.makePosition(centerX, centerY)
                }
            }
        }
    }
)
