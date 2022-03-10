/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty

/**
 * A [SteeringAction] in a vector space. The implementation of [nextPosition] is left to subclasses.
 */
abstract class AbstractSteeringAction<T, P, A>(
    environment: Environment<T, P>,
    /**
     * The reaction in which this action is executed.
     */
    protected open val reaction: Reaction<T>,
    /**
     * The owner of this action.
     */
    node: Node<T>,
) : AbstractMoveNode<T, P>(environment, node),
    SteeringAction<T, P>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P> {

    /**
     * The maximum distance the node can walk, this is a length.
     */
    open val maxWalk: Double get() = node.asProperty<T, PedestrianProperty<T>>().speed() / reaction.rate

    /**
     * @return The next position where to move, in absolute or relative coordinates depending on the
     *         value of isAbsolute.
     */
    override fun getNextPosition(): P = nextPosition()

    /**
     * This method allows to clone this action on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     *
     * @param [node]
     *            The node where to clone this {@link Action}
     * @param [reaction]
     *            The reaction to which the CURRENT action is assigned
     * @return the cloned action
     */
    abstract override fun cloneAction(node: Node<T>, reaction: Reaction<T>): AbstractSteeringAction<T, P, A>

    /**
     * Ensures that the passed [node] has type [N].
     */
    protected inline fun <reified N : Node<*>, S : Action<*>> requireNodeTypeAndProduce(
        node: Node<*>,
        builder: (N) -> S
    ): S {
        require(node is N) { "Incompatible node type. Required ${N::class}, found ${node::class}" }
        return builder(node)
    }
}
