/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Node

/**
 * This condition does not influence the reaction propensity,
 * it contributes respectively with 1.0 in case it can execute,
 * or with 0.0 in case it cannot.
 */
abstract class AbstractNonPropensityContributingCondition<T>(node: Node<T>) : AbstractCondition<T>(node) {
    final override fun getPropensityContribution(): Double = if (isValid) 1.0 else 0.0
}
