/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.cognitiveagents.NavigationStrategy
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A [NavigationStrategy] in a bidimensional euclidean space.
 */
typealias NavigationStrategy2D<T, L, R, N, E> =
NavigationStrategy<T, Euclidean2DPosition, Euclidean2DTransformation, L, R, N, E>
