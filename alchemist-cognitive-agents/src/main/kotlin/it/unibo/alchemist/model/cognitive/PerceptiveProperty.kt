/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.physics.InfluenceSphere

/**
 * The pedestrian's capability to perceive and influence other pedestrians.
 *
 * @param T the concentration type.
 */
interface PerceptiveProperty<T> : NodeProperty<T> {
    /** The agent's primary field of view. */
    val fieldOfView: InfluenceSphere<T>

    /**
     * The set of influence spheres associated with this pedestrian. By default only [fieldOfView] is present.
     */
    val senses: Map<String, InfluenceSphere<T>> get() = mapOf("view" to fieldOfView)
}
