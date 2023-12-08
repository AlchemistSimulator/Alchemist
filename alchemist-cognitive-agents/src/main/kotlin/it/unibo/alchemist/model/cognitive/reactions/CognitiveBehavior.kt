/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.CognitiveProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.reactions.AbstractReaction

/**
 * Reaction representing the cognitive behavior of a pedestrian.
 *
 * @param node
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to this the reaction executes.
 */
class CognitiveBehavior<T, V, A>(
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(node, timeDistribution)
    where V : Vector<V>, A : Transformation<V> {

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        CognitiveBehavior(node, timeDistribution)

    override fun updateInternalStatus(curTime: Time, executed: Boolean, environment: Environment<T, *>) =
        node.asProperty<T, CognitiveProperty<T>>().cognitiveModel.update(rate)
}
