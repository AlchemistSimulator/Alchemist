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
import it.unibo.alchemist.model.cognitive.PerceptiveProperty
import it.unibo.alchemist.model.physics.InfluenceSphere
import it.unibo.alchemist.model.properties.AbstractNodeProperty

/**
 * Base implementation of a pedestrian's capability to influence each other.
 */
data class Perceptive<T>(
    override val node: Node<T>,
    override val fieldOfView: InfluenceSphere<T>,
) : AbstractNodeProperty<T>(node), PerceptiveProperty<T> {
    override fun cloneOnNewNode(node: Node<T>) = Perceptive(node, fieldOfView)
}
