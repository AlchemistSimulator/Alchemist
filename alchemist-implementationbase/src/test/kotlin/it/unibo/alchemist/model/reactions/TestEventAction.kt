/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractAction

class TestEventAction<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    reaction: Reaction<T>,
) : AbstractAction<T>(reaction) {
    private var executed = false

    override fun cloneAction(newReaction: Reaction<T>): Action<T> = TestEventAction(environment, newReaction)

    override fun execute() {
        when (executed) {
            true -> error("Reaction already executed")
            false -> executed = true
        }
    }
}
