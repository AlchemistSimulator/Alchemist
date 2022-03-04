/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node

/**
 * A generic [Node].
 */
open class GenericNode<T>(
    /**
     * simulation incarnation.
     */
    val incarnation: Incarnation<T, *>,
    environment: Environment<*, *>
) : AbstractNode<T>(environment) {
    override fun createT(): T = incarnation.createConcentration()
}
