/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

/**
 * Represents a node seen by the [it.unibo.alchemist.model.actions.CameraSee] action.
 */
interface VisibleNode<T, P : Position<P>> {
    /**
     * The node seen.
     */
    val node: Node<T>

    /**
     * The position of the node.
     */
    val position: P
}
