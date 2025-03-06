/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model

import java.io.Serializable
import java.util.stream.Stream

/**
 * @param <P> type of Position followed by [Route]
 */
interface Route<P : Position<*>> :
    Iterable<P>,
    Serializable {
    /**
     * @return the length of the route
     */
    fun length(): Double

    /**
     * @param step the step
     * @return the step-th [Position] in the route
     */
    fun getPoint(step: Int): P

    /**
     * @return the route as list of [Position]
     */
    val points: List<P>

    /**
     * @return the route as stream of [Position]
     */
    @Deprecated("Use points.stream() instead", ReplaceWith("points.stream()"))
    fun stream(): Stream<P> = points.stream()

    /**
     * @return the number of points this route is made of
     */
    fun size(): Int
}
