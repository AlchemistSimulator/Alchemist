/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.device.properties

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position

class UpdateStatusProperty<T, P : Position<P>>(
    override val node: Node<T>,
    private val environment: Environment<T, P>,
) : NodeProperty<T> {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = UpdateStatusProperty(node, environment)
}
