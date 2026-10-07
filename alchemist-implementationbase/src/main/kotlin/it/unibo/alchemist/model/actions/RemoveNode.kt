/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction

/**
 * Removes the node owning this action from [environment].
 *
 * @param T concentration type
 */
class RemoveNode<T>(private val environment: Environment<T, *>, reaction: NodeReaction<T>) :
    AbstractLocalAction<T>(reaction) {
    override fun execute() = environment.removeNode(targetNode)

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): RemoveNode<T> = RemoveNode(environment, newReaction)

    override fun toString(): String = "Remove node ${targetNode.id}"
}
