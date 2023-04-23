/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import org.apache.commons.math3.random.RandomGenerator

/**
 * A cognitive pedestrian's movement capability.
 */
class CognitivePedestrian<T, S, A>(
    randomGenerator: RandomGenerator,
    node: Node<T>,
) : HeterogeneousPedestrian<T, S, A>(randomGenerator, node)
    where S : Vector<S>, A : Transformation<S> {

    private val cognitiveModel by lazy { node.asProperty<T, CognitiveProperty<T>>().cognitiveModel }

    override fun speed(): Double {
        return if (cognitiveModel.wantsToEscape()) {
            runningSpeed * minOf(cognitiveModel.escapeIntention(), 1.0)
        } else {
            walkingSpeed * minOf(cognitiveModel.remainIntention(), 1.0)
        }
    }
}
