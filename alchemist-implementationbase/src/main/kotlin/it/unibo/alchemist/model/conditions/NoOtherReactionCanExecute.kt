/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.conditions

import arrow.core.getOrElse
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.observables.util.Observables.combineLatest
import it.unibo.alchemist.model.observables.util.Observables.switchMap

/**
 * The condition is valid if all the other reactions having at least one condition can not execute.
 * This condition can be used only in a single reaction per node,
 * as multiple instances would lead to undecidable situations.
 */
class NoOtherReactionCanExecute<T>(reaction: NodeReaction<T>) : AbstractLocalCondition<T>(reaction) {
    init {
        require(
            targetNode.reactions.current
                .asSequence()
                .flatMap { it.conditions }
                .none { it is NoOtherReactionCanExecute<T> },
        ) {
            val className = this::class.simpleName
            "Violation of the $className contract. Only a single $className per node can get built. " +
                "Double creation at node $targetNode, reaction $reaction"
        }

        setValidity(
            targetNode.reactions.switchMap { reactions ->
                reactions
                    .filterNot { it == reaction }
                    .filter { it.conditions.isNotEmpty() }
                    .map { it.canExecute }
                    .combineLatest { reactionsCanExecute -> reactionsCanExecute.none { it } }
                    .map { it.getOrElse { true } }
            },
        )
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): NoOtherReactionCanExecute<T> =
        NoOtherReactionCanExecute(newReaction)
}
