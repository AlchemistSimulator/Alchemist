/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.geometry.GeometricShape
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A convex [GeometricShape].
 *
 * This interface models a property instead of an object, this may be
 * unusual but consider it as a contract: interfaces are often said to
 * be contracts the implementor has to comply, the contract defined by
 * this interface implies convexity.
 */
interface ConvexGeometricShape<V : Vector<V>, A : GeometricTransformation<V>> : GeometricShape<V, A>
