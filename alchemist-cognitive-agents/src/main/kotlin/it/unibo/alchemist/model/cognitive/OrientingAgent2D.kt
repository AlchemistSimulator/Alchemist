/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.geometry.Ellipse
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.jgrapht.graph.DefaultEdge

/**
 * An agent capable of orienting itself inside an Euclidean environment.
 */
typealias OrientingAgent2D = OrientingAgent<Euclidean2DPosition, Euclidean2DTransformation, Ellipse, DefaultEdge>
