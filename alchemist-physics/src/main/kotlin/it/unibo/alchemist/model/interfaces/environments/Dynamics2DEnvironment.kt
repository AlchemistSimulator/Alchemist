/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.euclidean.environments.Euclidean2DEnvironment
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment

/**
 * Any implementing Environment should take care of physical dynamics, in particular
 * collision detection and response.
 * This interface provides some hooks in order to be able to manage some of the node's physical state.
 */
interface Dynamics2DEnvironment<T> : Physics2DEnvironment<T>, Euclidean2DEnvironment<T> {

    /**
     * Set [node]'s current linear velocity.
     */
    fun setVelocity(node: Node<T>, velocity: Euclidean2DPosition)

    /**
     * Get [node]'s current linear velocity.
     */
    fun getVelocity(node: Node<T>): Euclidean2DPosition

    /**
     * Compute any collision response and update node positions.
     */
    fun updatePhysics(elapsedTime: Double)
}
