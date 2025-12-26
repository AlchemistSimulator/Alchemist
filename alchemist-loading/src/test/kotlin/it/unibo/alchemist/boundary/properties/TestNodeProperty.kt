/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.properties

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import org.apache.commons.math3.random.RandomGenerator

class TestNodeProperty<T, P : Position<P>>(
    val incarnation: Incarnation<T, P>,
    val rng: RandomGenerator,
    val environment: Environment<T, P>,
    node: Node<T>,
    val s: String,
) : AbstractNodeProperty<T>(node) {
    override fun cloneOnNewNode(node: Node<T>): NodeProperty<T> = TestNodeProperty(
        incarnation,
        rng,
        environment,
        node,
        s,
    )

    override fun toString(): String = super.toString() + "($s)"
}
