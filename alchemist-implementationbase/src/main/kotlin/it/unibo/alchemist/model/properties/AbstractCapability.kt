/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.properties

import it.unibo.alchemist.model.Capability
import it.unibo.alchemist.model.Node
import kotlin.reflect.jvm.jvmName

/**
 * Base class for [Capability] implementations, attached to [node].
 * Subclasses must define how they are cloned on a new node.
 */
abstract class AbstractCapability<T>(override val node: Node<T>) : Capability<T> {
    abstract override fun cloneOnNewNode(node: Node<T>): Capability<T>

    override fun toString(): String = with(this::class) { simpleName ?: qualifiedName ?: jvmName }
}
