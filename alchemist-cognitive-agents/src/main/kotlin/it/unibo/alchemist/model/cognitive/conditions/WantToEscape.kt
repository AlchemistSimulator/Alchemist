/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.conditions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.cognitive.properties.CognitiveProperty
import it.unibo.alchemist.model.conditions.AbstractLocalCondition
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * The intention of the pedestrian to evacuate or not.
 */
open class WantToEscape<T, S : Vector<S>, A : Transformation<S>>(reaction: NodeReaction<T>) :
    AbstractLocalCondition<T>(reaction) {
    init {
        val escapeDecision = targetNode.asProperty<T, CognitiveProperty<T>>().cognitiveModel.escapeDecision
        setValidity(escapeDecision)
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): WantToEscape<T, S, A> = WantToEscape(newReaction)
}
