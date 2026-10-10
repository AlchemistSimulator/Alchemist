/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.conditions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.EnvironmentNode
import it.unibo.alchemist.model.conditions.AbstractLocalCondition

/**
 * A condition valid when at least one [EnvironmentNode] is in the neighborhood of the node.
 */
class EnvPresent(private val environment: Environment<Double, *>, reaction: NodeReaction<Double>) :
    AbstractLocalCondition<Double>(reaction) {
    init {
        setValidity(
            environment.getNeighborhood(targetNode).map {
                it.neighbors.any { neighbor -> neighbor is EnvironmentNode }
            },
        )
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): EnvPresent =
        EnvPresent(environment, newReaction)

    override fun toString(): String = "has environment [${isValid.current}]"
}
