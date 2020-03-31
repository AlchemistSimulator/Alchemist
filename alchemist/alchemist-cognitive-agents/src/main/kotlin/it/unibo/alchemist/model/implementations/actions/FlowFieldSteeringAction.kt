package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.surrounding
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironment
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
 * @param selectPosition
 *          the logic according to the target position is determined from all the positions checked.
 */
open class FlowFieldSteeringAction<T>(
    private val env: EuclideanPhysics2DEnvironment<T>,
    reaction: Reaction<T>,
    private val pedestrian: Pedestrian2D<T>,
    private val targetMolecule: Molecule,
    private val selectPosition: Iterable<Euclidean2DPosition>.(Molecule) -> Euclidean2DPosition,
    targetSelectionStrategy: TargetSelectionStrategy<Euclidean2DPosition>
) : SteeringActionImpl<T, Euclidean2DPosition>(
    env,
    reaction,
    pedestrian,
    targetSelectionStrategy
) {

    override fun interpolatePositions(
        current: Euclidean2DPosition,
        target: Euclidean2DPosition,
        maxWalk: Double
    ): Euclidean2DPosition =
        current.surrounding(env, maxWalk)
                .filter { env.canNodeFitPosition(pedestrian, it) }
                .toMutableList()
                .selectPosition(targetMolecule) - current
}
