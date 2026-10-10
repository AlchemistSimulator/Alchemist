/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.TimeDistributedReaction
import it.unibo.alchemist.model.actions.AbstractMoveNode
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.properties.PedestrianProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A [SteeringAction] in a vector space. The implementation of [nextPosition] is left to subclasses.
 */
abstract class AbstractSteeringAction<T, P, A>(
    environment: Environment<T, P>,
    /**
     * The reaction in which this action is executed.
     */
    reaction: NodeReaction<T>,
    /**
     * The pedestrian property of the owner of this action.
     */
    protected val pedestrian: PedestrianProperty<T>,
) : AbstractMoveNode<T, P>(environment, reaction),
    SteeringAction<T, P>
    where P : Position<P>,
          P : Vector<P>,
          A : Transformation<P> {
    /** The recurrence rate required to normalize per-execution movement. */
    protected val recurrenceRate: Double =
        requireNotNull(reaction as? TimeDistributedReaction<*>) {
            "$reaction does not expose a recurrence rate"
        }.rate

    /**
     * The maximum distance the node can walk, this is a length.
     */
    open val maxWalk: Double get() = pedestrian.speed() / recurrenceRate

    /**
     * @return The next position where to move, in absolute or relative coordinates depending on the
     *         value of isAbsolute.
     */
    override fun getNextPosition(): P = nextPosition()

    /**
     * Creates an equivalent action owned by [newReaction], bound to its node.
     */
    abstract override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): AbstractSteeringAction<T, P, A>

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
