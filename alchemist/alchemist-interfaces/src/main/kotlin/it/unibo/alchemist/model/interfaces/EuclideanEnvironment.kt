/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * An Euclidean space, where [Position]s [P] are valid [Vector]s,
 * supporting any concentration type [T].
 */
interface EuclideanEnvironment<T, P> : Environment<T, P>
where P : Position<P>, P : Vector<P> {

    /**
     * Creates a [Position] compatible with this environment given its [coordinates].
     */
    fun makePosition(vararg coordinates: Double): P

    /**
     * Create a position corresponding to the origin of this environment.
     */
    val origin: P get() = makePosition(*DoubleArray(dimensions).toTypedArray())
}
