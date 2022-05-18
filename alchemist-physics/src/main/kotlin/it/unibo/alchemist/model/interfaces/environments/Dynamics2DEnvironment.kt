/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.environments

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node

interface Dynamics2DEnvironment<T> : Physics2DEnvironment<T>, Euclidean2DEnvironment<T> {

    fun setVelocity(node: Node<T>, velocity: Euclidean2DPosition)

    fun updatePhysics(elapsedTime: Double)
}
