package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

/**
 * Combination of multiple steering actions.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param node
 *          the owner of this action.
 * @param actions
 *          the list of actions to combine to determine the node movement.
 * @param steerStrategy
 *          the logic according to the steering actions are combined.
 */
class CognitiveAgentCombineSteering<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentCombineSteering<T, P, A> =
        CognitiveAgentCombineSteering(environment, reaction, node.pedestrianProperty, actions, steerStrategy)

    override fun nextPosition(): P = steerStrategy.computeNextPosition(actions).coerceAtMost(maxWalk)
}
