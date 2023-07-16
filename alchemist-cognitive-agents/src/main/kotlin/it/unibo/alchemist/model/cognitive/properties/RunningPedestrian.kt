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
import it.unibo.alchemist.model.cognitive.RunningPedestrianProperty
import it.unibo.alchemist.model.cognitive.impact.individual.Speed
import it.unibo.alchemist.model.properties.AbstractNodeProperty

/**
 * Implementation of a basic [RunningPedestrianProperty].
 */
data class RunningPedestrian<T> @JvmOverloads constructor(
    override val node: Node<T>,
    override val runningSpeed: Double = Speed.default * 3,
) : AbstractNodeProperty<T>(node), RunningPedestrianProperty<T> {

    override fun cloneOnNewNode(node: Node<T>) = RunningPedestrian(node, runningSpeed)
}
