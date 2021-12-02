/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A pedestrian with cognitive characteristics that lives in a bidimensional environment.
 */
interface CognitivePedestrian2D<T> :
    CognitivePedestrian<T, Euclidean2DPosition, Euclidean2DTransformation>,
    Pedestrian2D<T>
