/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.properties.CognitiveProperty
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty

/**
 * A cognitive pedestrian's movement capability.
 */
class MotileCognitivePedestrian<T, S, A>(
    randomGenerator: RandomGenerator,
    node: Node<T>,
) : MotileHeterogeneousPedestrian<T, S, A>(randomGenerator, node)
    where S : Vector<S>, A : GeometricTransformation<S> {
    override fun speed(): Double {
        val myCognitiveModel = node.asProperty<T, CognitiveProperty<T>>().cognitiveModel
        return if (myCognitiveModel.wantsToEscape()) {
            runningSpeed * minOf(myCognitiveModel.escapeIntention(), 1.0)
        } else {
            walkingSpeed * minOf(myCognitiveModel.remainIntention(), 1.0)
        }
    }
}
