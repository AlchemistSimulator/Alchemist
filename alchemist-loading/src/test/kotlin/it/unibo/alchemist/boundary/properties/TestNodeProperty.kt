/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.properties.AbstractNodeProperty

class TestNodeProperty<T>(node: Node<T>, val s: String) : AbstractNodeProperty<T>(node) {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = TestNodeProperty(node, s)

    override fun toString(): String = super.toString() + "($s)"
}
