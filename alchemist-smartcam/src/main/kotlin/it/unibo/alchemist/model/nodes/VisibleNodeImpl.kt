/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.nodes

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.VisibleNode

/**
 * Basic implementation of [VisibleNode].
 */
class VisibleNodeImpl<T, P : Position<P>>(
    override val node: Node<T>,
    override val position: P,
) : VisibleNode<T, P> {

    override fun equals(other: Any?) = other is VisibleNodeImpl<*, *> && other.node == node

    override fun hashCode() = node.hashCode()

    override fun toString() = "Visible#${node.id}"
}
