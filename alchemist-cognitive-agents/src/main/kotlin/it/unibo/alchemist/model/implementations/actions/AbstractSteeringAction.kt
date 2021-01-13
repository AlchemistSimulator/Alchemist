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
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [SteeringAction] in a vector space. The implementation of [nextPosition] is left to subclasses.
 */
abstract class AbstractSteeringAction<T, P, A>(
    env: Environment<T, P>,
    /**
     * The reaction in which this action is executed.
     */
    protected open val reaction: Reaction<T>,
    /**
     * The owner of this action.
     */
    protected open val pedestrian: Pedestrian<T, P, A>
) : AbstractMoveNode<T, P>(env, pedestrian),
    SteeringAction<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P> {

    /**
     * The maximum distance the pedestrian can walk, this is a length.
     */
    open val maxWalk: Double get() = pedestrian.speed() / reaction.rate

    override fun getNextPosition(): P = nextPosition()

    override fun getNode(): Pedestrian<T, P, A> = pedestrian

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): Action<T> =
        requireNodeTypeAndProduce<Pedestrian<T, P, A>, AbstractSteeringAction<T, P, A>>(node) {
            cloneAction(it, reaction)
        }

    protected abstract fun cloneAction(n: Pedestrian<T, P, A>, r: Reaction<T>): AbstractSteeringAction<T, P, A>

    protected inline fun <reified N : Node<*>, S : Action<*>> requireNodeTypeAndProduce(
        node: Node<*>,
        builder: (N) -> S
    ): S {
        require(node is N) { "Incompatible node type. Required ${N::class}, found ${node::class}" }
        return builder(node)
    }
}
