package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.actions.utils.surrounding
import it.unibo.alchemist.model.implementations.layers.BidimensionalGaussianLayer
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.*
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
import it.unibo.alchemist.model.interfaces.movestrategies.SpeedSelectionStrategy
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * Generic implementation of an action influenced by the concentration of a given molecule in the environment.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetMolecule
 *          the {@link Molecule} you want to know the concentration in the different positions of the environment.
 * @param formula
 *          the logic according to the target position is determined from all the positions checked.
 */
open class GradientSteeringAction<T>(
    private val env: EuclideanPhysics2DEnvironment<T>,
    pedestrian: Pedestrian2D<T>,
    private val targetMolecule: Molecule,
    private val formula: Iterable<Euclidean2DPosition>.(Molecule) -> Euclidean2DPosition
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    pedestrian,
    TargetSelectionStrategy {
        with(env.getLayer(targetMolecule).get() as BidimensionalGaussianLayer) {
            Euclidean2DPosition(centerX, centerY)
        }
    },
    SpeedSelectionStrategy { pedestrian.walkingSpeed }
) {
    override fun getDestination(current: Euclidean2DPosition, target: Euclidean2DPosition, maxWalk: Double) =
        current.surrounding(env, maxWalk, 8).formula(targetMolecule)
}