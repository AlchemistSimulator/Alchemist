/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape

/**
 * A node's capability to exist with a shape in a 2D space.
 */
class BaseSpatial2DCapability<T> @JvmOverloads constructor(
    environment: Physics2DEnvironment<T>,
    override val node: Node<T>,
    override val shape: Euclidean2DShape = environment.shapeFactory.circle(SpatialCapability.defaultShapeRadius),
) : Spatial2DCapability<T>
