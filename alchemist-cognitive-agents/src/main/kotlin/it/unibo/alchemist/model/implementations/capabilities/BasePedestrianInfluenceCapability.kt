/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.InfluenceSphere2D
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.capabilities.PedestrianInfluenceCapability
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.InfluenceSphere

/**
 * Base implementation of a pedestrian's capability to influence each other.
 */
data class BasePedestrianInfluenceCapability<T> (
    override val node: Node<T>,
    override val fieldOfView: InfluenceSphere,
) : PedestrianInfluenceCapability<T>

/**
 * Base implementation of a pedestrian's capability to influence each other in a 2D space.
 */
class BasePedestrian2DInfluenceCapability<T> @JvmOverloads constructor(
    environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    override val fieldOfView: InfluenceSphere2D<T> =
        FieldOfView2D(environment, node, defaultFieldOfViewDepth, defaultFieldOfViewAperture),
) : PedestrianInfluenceCapability<T> by BasePedestrianInfluenceCapability(
    node,
    fieldOfView,
) {
    companion object {
        /**
         * Default aperture of pedestrian's [fieldOfView].
         */
        const val defaultFieldOfViewAperture = Math.PI / 180 * 80
        /**
         * Default depth of pedestrian's [fieldOfView].
         */
        const val defaultFieldOfViewDepth = 10.0
    }
}
