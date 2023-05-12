/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment

/**
 * A sphere of influence representing the sight of a node in the Euclidean world.
 *
 * @param environment
 *          the environment where this sphere of influence is.
 * @param owner
 *          the node who owns this sphere of influence.
 * @param distance
 *          the distance in meters at which the sight arrives.
 * @param aperture
 *          the amplitude of the field of view in radians.
 */
class FieldOfView2D<T>(
    environment: Physics2DEnvironment<T>,
    owner: Node<T>,
    distance: Double,
    aperture: Double,
) : InfluenceSphere2D<T>(environment, owner, environment.shapeFactory.circleSector(distance, aperture, 0.0))
