/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import org.apache.commons.math3.random.RandomGenerator

/**
 * A local action relying on a [randomGenerator].
 *
 * @param T concentration type
 */
abstract class AbstractRandomizableAction<T>(
    reaction: NodeReaction<T>,
    /**
     * The random generator used by this action.
     */
    protected val randomGenerator: RandomGenerator,
) : AbstractLocalAction<T>(reaction)
