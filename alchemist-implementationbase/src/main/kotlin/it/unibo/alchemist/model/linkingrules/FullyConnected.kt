/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.neighborhoods.Neighborhoods

/**
 * This rule connects each and every node to each and every other.
 */
class FullyConnected<T, P : Position<P>> : LinkingRule<T, P> {
    override fun isLocallyConsistent() = true

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>) =
        Neighborhoods.make(environment, center, environment.nodes.filter { it != center })
}
