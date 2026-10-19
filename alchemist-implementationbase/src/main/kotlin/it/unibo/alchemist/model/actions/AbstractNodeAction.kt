/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.reactions.retarget

/**
 * Base implementation of an [Action] executed on a target [targetNode], owned by a [reaction] with any host.
 *
 * When cloned, an action targeting the host of its reaction targets the host of the new reaction, which must be a
 * node; an action targeting any other node keeps its target.
 */
abstract class AbstractNodeAction<T>(reaction: Reaction<T>, val targetNode: Node<T>) : AbstractAction<T>(reaction) {
    final override fun cloneAction(newReaction: Reaction<T>): Action<T> =
        cloneTargeting(newReaction, reaction.retarget(targetNode, newReaction))

    /**
     * Creates an equivalent action owned by [newReaction] and executed on [newTarget].
     */
    protected abstract fun cloneTargeting(newReaction: Reaction<T>, newTarget: Node<T>): Action<T>
}
