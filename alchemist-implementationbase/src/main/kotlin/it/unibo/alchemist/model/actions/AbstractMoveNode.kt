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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position

/**
 * A node-bound action moving its node in [environment].
 *
 * @param T concentration type
 * @param P position type
 * @property environment the environment where the node moves
 * @property isAbsolute whether [getNextPosition] returns absolute positions. If false, it returns a displacement
 * relative to the current position: for instance, a node in (1,1) moving to (2,3) gets (1,2).
 */
abstract class AbstractMoveNode<T, P : Position<P>> @JvmOverloads protected constructor(
    open val environment: Environment<T, P>,
    reaction: NodeReaction<T>,
    protected val isAbsolute: Boolean = false,
) : AbstractLocalAction<T>(reaction) {
    /**
     * The position of the node owning this action.
     */
    protected val currentPosition: P get() = getNodePosition(targetNode)

    /**
     * Moves the node according to [getNextPosition], in absolute or relative coordinates depending on [isAbsolute].
     */
    override fun execute() {
        val nextPosition = getNextPosition()
        environment.moveNodeTo(
            targetNode,
            if (isAbsolute) nextPosition else currentPosition.plus(nextPosition.coordinates),
        )
    }

    /**
     * The next position to reach, in absolute or relative coordinates depending on [isAbsolute].
     */
    abstract fun getNextPosition(): P

    /**
     * The current position of [node].
     */
    protected fun getNodePosition(node: Node<T>): P = environment.getCurrentPosition(node)
}
