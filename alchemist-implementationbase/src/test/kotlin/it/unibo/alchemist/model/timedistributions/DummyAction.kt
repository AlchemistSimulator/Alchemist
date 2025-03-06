/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.timedistributions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.molecules.SimpleMolecule

class DummyAction(
    node: Node<Int>,
) : AbstractLocalAction<Int>(node) {
    override fun cloneAction(node: Node<Int>, reaction: Reaction<Int>): Action<Int> = DummyAction(node)

    override fun execute() {
        // Do nothing, this is a dummy action
        node.setConcentration(SimpleMolecule("dummy"), 0)
    }
}
