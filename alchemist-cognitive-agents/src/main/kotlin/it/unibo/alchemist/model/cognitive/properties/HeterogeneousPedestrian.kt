/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.cognitive.HumanProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import org.apache.commons.math3.random.RandomGenerator

/**
 * A heterogeneous pedestrian's movement capability.
 * Note: to use this capability the node must already have a [HumanProperty].
 */
open class HeterogeneousPedestrian<T, S, A> (
    randomGenerator: RandomGenerator,
    node: Node<T>,
) : Pedestrian<T>(
    randomGenerator,
    node,
) where S : Vector<S>, A : Transformation<S> {

    private val human by lazy { node.asProperty<T, HumanProperty<T, S, A>>() }

    override val walkingSpeed: Double get() = human.speed.walking

    override val runningSpeed: Double get() = human.speed.running

    override fun toString() = "HeterogenousPedestrian${node.id}"
}
