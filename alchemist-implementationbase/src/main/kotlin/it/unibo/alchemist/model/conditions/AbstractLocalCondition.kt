/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Reaction

/**
 * Base implementation of a [Condition] evaluated on the node hosting its [NodeReaction].
 *
 * A local condition can only be cloned onto a [NodeReaction], and it then targets the node of that reaction.
 */
abstract class AbstractLocalCondition<T>(reaction: NodeReaction<T>) :
    AbstractNodeCondition<T>(reaction, reaction.host) {
    /**
     * The [NodeReaction] owning this condition, hosted by [targetNode].
     */
    // Narrows the stored reaction rather than storing it twice: the constructor only accepts node reactions.
    final override val reaction: NodeReaction<T> get() = super.reaction as NodeReaction<T>

    final override fun cloneTargeting(newReaction: Reaction<T>, newTarget: Node<T>): Condition<T> {
        require(newReaction is NodeReaction<T>) {
            "$this is local to a node reaction and cannot be cloned onto $newReaction"
        }
        return cloneOnNodeReaction(newReaction)
    }

    /**
     * Creates an equivalent condition owned by [newReaction], evaluated on its node.
     */
    protected abstract fun cloneOnNodeReaction(newReaction: NodeReaction<T>): Condition<T>
}
