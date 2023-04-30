/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.euclidean.geometry.navigationgraph

import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition

/**
 * A 2D [DirectedNavigationGraph].
 */
typealias DirectedEuclidean2DNavigationGraph =
DirectedNavigationGraph<Euclidean2DPosition, Euclidean2DTransformation, ConvexPolygon, Euclidean2DPassage>
