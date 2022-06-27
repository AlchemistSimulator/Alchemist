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
import it.unibo.alchemist.model.interfaces.properties.HumanProperty
import org.apache.commons.math3.random.RandomGenerator
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A heterogeneous pedestrian's movement capability.
 * Note: to use this capability the node must already have a [HumanProperty].
 */
open class HeterogeneousPedestrian<T, S, A> (
    randomGenerator: RandomGenerator,
    node: Node<T>,
) : Pedestrian<T> (
    randomGenerator,
    node,
) where S : Vector<S>, A : GeometricTransformation<S> {

    private val human by lazy { node.asProperty<T, HumanProperty<T, S, A>>() }

    override val walkingSpeed: Double get() = human.speed.walking

    override val runningSpeed: Double get() = human.speed.running

    override fun toString() = "HeterogenousPedestrian${node.id}"
}
