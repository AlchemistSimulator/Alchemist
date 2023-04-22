/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitiveagents.SteeringAction
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty

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
     * The pedestrian property of the owner of this action.
     */
    protected open val pedestrian: PedestrianProperty<T>,
) : AbstractMoveNode<T, P>(environment, pedestrian.node),
    SteeringAction<T, P>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P> {

    /**
     * The maximum distance the node can walk, this is a length.
     */
    open val maxWalk: Double get() = pedestrian.speed() / reaction.rate

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
        builder: (N) -> S,
    ): S {
        require(node is N) { "Incompatible node type. Required ${N::class}, found ${node::class}" }
        return builder(node)
    }

    /**
     * Get the pedestrian property. This can be useful when cloning actions this actions.
     */
    protected val Node<T>.pedestrianProperty get() = this.asProperty<T, PedestrianProperty<T>>()
}
