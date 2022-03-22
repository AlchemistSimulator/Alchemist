package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Move the node towards the target position as fast as possible.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param node
 *          the owner of this action.
 * @param target
 *          the position the node moves towards.
 */
open class CognitiveAgentSeek<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    node: Node<T>,
    target: P
) : CognitiveAgentArrive<T, P, A>(environment, reaction, node, 0.0, 0.0, target)
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        node: Node<T>,
        vararg coordinates: Number
    ) : this(environment, reaction, node, environment.makePosition(*coordinates))

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeek<T, P, A> =
        CognitiveAgentSeek(environment, reaction, node, target)
}
