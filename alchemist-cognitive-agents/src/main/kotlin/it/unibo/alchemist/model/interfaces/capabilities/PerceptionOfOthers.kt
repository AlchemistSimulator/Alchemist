/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.capabilities

import it.unibo.alchemist.model.interfaces.Capability
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere

/**
 * The pedestrian's capability to influence other pedestrians.
 */
interface PerceptionOfOthers<T> : Capability<T> {

    /**
     * The field of view of the pedestrian.
     */
    val fieldOfView: InfluenceSphere<T>

    /**
     * The list of influence spheres belonging to this pedestrian (by default, only its [fieldOfView]).
     */
    val senses: Map<String, InfluenceSphere<T>> get() = mapOf("view" to fieldOfView)
}
