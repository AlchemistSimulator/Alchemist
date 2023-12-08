/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry.navigationgraph

import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A directed unweighted [BaseNavigationGraph], allowing multiple edges between the
 * same pair of vertices and without self-loops (i.e. edges connecting a node to
 * itself).
 */
class DirectedNavigationGraph<V, A, N, E>(
    edgeClass: Class<out E>,
) : BaseNavigationGraph<V, A, N, E>(edgeClass, true)
    where V : Vector<V>,
          A : Transformation<V>,
          N : ConvexShape<V, A>
