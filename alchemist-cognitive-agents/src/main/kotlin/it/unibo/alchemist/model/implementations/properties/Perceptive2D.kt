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
import it.unibo.alchemist.model.environments.Physics2DEnvironment
import it.unibo.alchemist.model.geometry.euclidean2d.FieldOfView2D
import it.unibo.alchemist.model.geometry.euclidean2d.InfluenceSphere2D
import it.unibo.alchemist.model.interfaces.properties.PerceptiveProperty

/**
 * Base implementation of a pedestrian's capability to influence each other in a 2D space.
 */
class Perceptive2D<T> @JvmOverloads constructor(
    /**
     * The environment where [node] is moving.
     */
    val environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    override val fieldOfView: InfluenceSphere2D<T> =
        FieldOfView2D(environment, node, defaultFieldOfViewDepth, defaultFieldOfViewAperture),
) : PerceptiveProperty<T> by Perceptive(
    node,
    fieldOfView,
) {
    override fun cloneOnNewNode(node: Node<T>) = Perceptive2D(
        environment,
        node,
        FieldOfView2D(environment, node, defaultFieldOfViewDepth, defaultFieldOfViewAperture),
    )

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
