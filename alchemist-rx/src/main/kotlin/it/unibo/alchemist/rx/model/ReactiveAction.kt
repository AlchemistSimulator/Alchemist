/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.rx.model

import it.unibo.alchemist.rx.model.adapters.ObservableNode
import java.io.Serializable

/**
 * Similar to [it.unibo.alchemist.model.Action], with the difference that
 * this must be used in observable environment; therefore, no context
 * nor dependencies are needed. Finally, the [cloneAction] method is
 * now supporting observable and reactive structures.
 */
interface ReactiveAction<T> : Serializable {

    /**
     * This method allows to clone this action on a new node. It may result
     * useful to support runtime creation of nodes with the same reaction
     * programming, e.g. for morphogenesis.
     *
     * @param node The node[ObservableNode] where to clone this [ReactiveAction]
     * @para reaction the [reaction][ReactiveReaction] to which the CURRENT action is assigned.
     * @return the cloned action
     */
    fun cloneAction(node: ObservableNode<T>, reaction: ReactiveReaction<T>): ReactiveAction<T>

    /**
     * Effectively executes this action.
     */
    fun execute()
}
