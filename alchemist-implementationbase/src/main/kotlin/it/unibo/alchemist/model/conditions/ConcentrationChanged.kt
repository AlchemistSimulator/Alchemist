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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observation.MutableObservable

/** A condition that becomes valid when the concentration of [target] changes. */
class ConcentrationChanged<T>(node: Node<T>, private val target: Molecule) : AbstractCondition<T>(node) {
    private val resets = MutableObservable.observe(0L)
    private var previous: T? = node.getConcentration(target)
    private var changed = false

    init {
        setValidity(
            node.observeConcentration(target).mergeWith(resets) { concentration, _ ->
                if (!changed) {
                    val current = concentration.getOrNull()
                    if (current != previous) {
                        changed = true
                        previous = current
                    }
                }
                changed
            },
        )
    }

    override fun cloneCondition(newNode: Node<T>, newReaction: NodeReaction<T>): ConcentrationChanged<T> =
        ConcentrationChanged(newNode, target)

    override fun reactionReady() {
        changed = false
        resets.current++
    }

    override fun toString(): String = "$target changes value"
}
