/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

/**
 * A [SteeringAction] allowing to navigate the environment consciously (e.g. without remaining
 * blocked in U-shaped obstacles). To put it another way, [nextPosition] should be computed taking
 * into account the perceptible spatial structure of the environment and other spatial information
 * available.
 */
interface OrientingAction<T, P : Position<P>> : SteeringAction<T, P>
