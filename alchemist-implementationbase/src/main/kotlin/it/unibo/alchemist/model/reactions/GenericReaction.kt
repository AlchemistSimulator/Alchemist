/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.reactions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution

/**
 * A generic reaction gated by condition validity that schedules a new sample after every occurrence.
 *
 * @param T concentration type
 * @param node node hosting the reaction
 * @param timeDistribution delay generator
 */
open class GenericReaction<T>(node: Node<T>, timeDistribution: TimeDistribution<T>) :
    AbstractNodeReaction<T>(node, timeDistribution) {

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): GenericReaction<T> =
        makeClone(node, currentTime) { freshGenerator -> GenericReaction(node, freshGenerator) }
}
