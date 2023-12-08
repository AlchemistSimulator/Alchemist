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
import it.unibo.alchemist.model.cognitive.WalkingPedestrianProperty
import it.unibo.alchemist.model.cognitive.impact.individual.Speed
import it.unibo.alchemist.model.properties.AbstractNodeProperty

/**
 * Implementation of a basic [WalkingPedestrianProperty].
 */
data class WalkingPedestrian<T> @JvmOverloads constructor(
    override val node: Node<T>,
    override val walkingSpeed: Double = Speed.default,
) : AbstractNodeProperty<T>(node), WalkingPedestrianProperty<T> {

    override fun cloneOnNewNode(node: Node<T>) = WalkingPedestrian(node, walkingSpeed)
}
