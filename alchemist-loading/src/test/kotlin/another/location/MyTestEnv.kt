/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package another.location

import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.implementations.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

class MyTestEnv<T> (incarnation: Incarnation<T, Euclidean2DPosition>) : Continuous2DEnvironment<T>(incarnation)
