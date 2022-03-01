package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringActionWithTarget
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.movestrategies.TargetSelectionStrategy

/**
 * A [SteeringActionWithTarget] in a vector space.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetSelectionStrategy
 *          strategy selecting the next target.
 */
abstract class AbstractSteeringActionWithTarget<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Node<T>,
    private val targetSelectionStrategy: TargetSelectionStrategy<T, P>
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    SteeringActionWithTarget<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: Node<T>,
        target: P
    ) : this(environment, reaction, pedestrian, TargetSelectionStrategy { target })

    override fun target(): P = targetSelectionStrategy.target

    /**
     * @returns the next relative position. By default, the pedestrian tries to move towards its [target].
     */
    override fun nextPosition(): P = (target() - currentPosition).coerceAtMost(maxWalk)
}
