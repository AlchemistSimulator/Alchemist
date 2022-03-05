package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Combination of multiple steering actions.
 *
 * @param env
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this action.
 * @param actions
 *          the list of actions to combine to determine the pedestrian movement.
 * @param steerStrategy
 *          the logic according to the steering actions are combined.
 */
class CognitiveAgentCombineSteering<T, P, A>(
    private val env: Environment<T, P>,
    reaction: Reaction<T>,
    pedestrian: Node<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>
) : AbstractSteeringAction<T, P, A>(env, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentCombineSteering<T, P, A> =
        CognitiveAgentCombineSteering(env, reaction, node, actions, steerStrategy)

    override fun nextPosition(): P = steerStrategy.computeNextPosition(actions).coerceAtMost(maxWalk)
}
