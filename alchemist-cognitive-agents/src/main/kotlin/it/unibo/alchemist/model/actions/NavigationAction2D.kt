/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.NavigationAction
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * A [NavigationAction] in a bidimensional euclidean space.
 */
typealias NavigationAction2D<T, L, R, N, E> =
NavigationAction<T, Euclidean2DPosition, Euclidean2DTransformation, L, R, N, E>
