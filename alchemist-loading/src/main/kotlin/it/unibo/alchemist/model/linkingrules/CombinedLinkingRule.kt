/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
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
 * A meta-rule that combines multiple [subRules].
 * If any mandates a link, such link is created (union of all links).
 */
class CombinedLinkingRule<T, P : Position<P>>(
    val subRules: List<LinkingRule<T, P>>,
) : LinkingRule<T, P> {

    private val isConsistent by lazy { subRules.all { it.isLocallyConsistent } }

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>) = Neighborhoods.make(
        environment,
        center,
        // Add all neigbours as per subrule
        subRules.asSequence().flatMap { it.computeNeighborhood(center, environment).neighbors }.asIterable(),
    )

    override fun isLocallyConsistent() = isConsistent
}
