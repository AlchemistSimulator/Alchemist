/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry

import it.unibo.alchemist.model.geometry.Shape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A convex [Shape].
 *
 * This interface models a property instead of an object, this may be
 * unusual but consider it as a contract: interfaces are often said to
 * be contracts the implementor has to comply, the contract defined by
 * this interface implies convexity.
 */
interface ConvexShape<V : Vector<V>, A : Transformation<V>> : Shape<V, A>
