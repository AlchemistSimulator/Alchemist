/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observables.util.MutableObservables.observe

/** A condition that becomes valid when the concentration of [target] changes. */
class ConcentrationChanged<T>(reaction: NodeReaction<T>, private val target: Molecule) :
    AbstractLocalCondition<T>(reaction) {
    private val resets = observe(0L)

    // Options, because an absent molecule must neither read as the default nor as a present null.
    private var previous = targetNode.observeConcentration(target).current
    private var changed = false

    init {
        setValidity(
            targetNode.observeConcentration(target).mergeWith(resets) { concentration, _ ->
                if (!changed && concentration != previous) {
                    changed = true
                    previous = concentration
                }
                changed
            },
        )
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): ConcentrationChanged<T> =
        ConcentrationChanged(newReaction, target)

    override fun beforeReactionFires() {
        changed = false
        resets.current++
    }

    override fun toString(): String = "$target changes value"
}
