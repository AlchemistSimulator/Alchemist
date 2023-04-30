/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.environments

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Implementation of [Euclidean2DEnvironment].
 */
open class Continuous2DEnvironment<T>(incarnation: Incarnation<T, Euclidean2DPosition>) :
    Euclidean2DEnvironment<T>,
    Abstract2DEnvironment<T, Euclidean2DPosition>(incarnation)
