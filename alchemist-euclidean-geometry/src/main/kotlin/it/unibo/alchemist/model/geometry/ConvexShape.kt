/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

/**
 * A convex [Shape].
 *
 * This interface models a property instead of an object, this may be
 * unusual but consider it as a contract: interfaces are often said to
 * be contracts the implementor has to comply, the contract defined by
 * this interface implies convexity.
 */
interface ConvexShape<V : Vector<V>, A : Transformation<V>> : Shape<V, A>
