/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction

/**
 * Chooses the target node for the clone of a condition or action that this reaction owns and that targets [target].
 *
 * Cloning moves the condition or action from this reaction to [newReaction], typically because a node is being
 * cloned. The clone targets:
 * - the host of [newReaction], if [target] is the host of this reaction, so that an element working on its own node
 *   keeps working on its own node after cloning;
 * - [target] itself otherwise, so that an element working on some other node keeps working on that same node.
 *
 * For example, a `BrownianMove` moving the node hosting its reaction moves the new node once cloned, whereas one
 * moving another node keeps moving that node.
 *
 * @throws IllegalStateException if [target] is the host of this reaction but [newReaction] is not hosted by a node.
 */
internal fun <T> Reaction<T>.retarget(target: Node<T>, newReaction: Reaction<T>): Node<T> = when {
    // The element works on a node other than the host of its reaction: the clone keeps working on that node.
    target !== host -> target
    // The element works on the host of its reaction: the clone works on the host of the new reaction.
    newReaction.host is Node<T> -> newReaction.host as Node<T>
    // The element works on the host of its reaction, but the new reaction has no node to work on.
    else -> error(
        "The clone of an element targeting the host of $this requires a node host, " +
            "but $newReaction is hosted by ${newReaction.host}",
    )
}
