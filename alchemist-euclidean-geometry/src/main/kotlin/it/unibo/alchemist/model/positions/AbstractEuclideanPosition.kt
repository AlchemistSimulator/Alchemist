/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.positions

import it.unibo.alchemist.model.geometry.Vector

/**
 * N-dimensional Euclidean position.
 *
 * @param P actual type
 * @param copy true if it is unsafe to store the [coordinates] array as-is.
 * @param coordinates the array of coordinates.
 */
// It cannot be concrete: it inherits the abstract fromCoordinates from AbstractPosition, which detekt does not see.
@Suppress("AbstractClassCanBeConcreteClass")
abstract class AbstractEuclideanPosition<P : AbstractEuclideanPosition<P>> protected constructor(
    copy: Boolean,
    coordinates: DoubleArray,
) : AbstractPosition<P>(copy, coordinates),
    Vector<P>
