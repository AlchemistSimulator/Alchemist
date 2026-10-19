/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Reaction

/**
 * Base implementation of an [Action] owned by [reaction], independent of the kind of host.
 */
abstract class AbstractAction<T>(override val reaction: Reaction<T>) : Action<T> {
    override fun toString(): String = this::class.simpleName ?: "<anonymous ${this::class}>"
}
