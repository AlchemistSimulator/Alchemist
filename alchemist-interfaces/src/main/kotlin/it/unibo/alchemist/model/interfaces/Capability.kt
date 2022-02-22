/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

/**
 * Represents a node's capability.
 */
interface Capability<T> {
    /**
     * The node to which the capability is added.
     */
    val node: Node<T>

    /**
     * Creates an empty base molecule.
     */
    fun createMolecule(): T? = null
}
