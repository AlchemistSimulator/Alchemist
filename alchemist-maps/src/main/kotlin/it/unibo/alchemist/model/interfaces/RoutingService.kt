/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.Position
import java.io.Serializable

/**
 * A service capable to generate routes into an environment.
 * Parametric on the [Position] type [P] and the supported [RoutingServiceOptions] [O].
 */
interface RoutingService<P : Position<P>, O : RoutingServiceOptions<O>> : Serializable {

    /**
     * The default set of options.
     */
    val defaultOptions: O

    /**
     * Retrieves (if available) the valid point closest to [position], using the default options.
     */
    fun allowedPointClosestTo(position: P): P? = allowedPointClosestTo(position, defaultOptions)

    /**
     * Retrieves (if available) the valid point closest to [position] with the provided [options].
     * For instance, this method could be used to find the pedestrian-allowed road closer to a highway.
     */
    fun allowedPointClosestTo(position: P, options: O): P?

    /**
     * Computes a [Route] [from] a [P] [to] another.
     */
    fun route(from: P, to: P): Route<P> = route(from, to, defaultOptions)

    /**
     * Computes a [Route] [from] a [P] [to] another, honoring the provided set of navigation [options].
     */
    fun route(from: P, to: P, options: String): Route<P> = route(from, to, parseOptions(options))

    /**
     * Computes a [Route] [from] a [P] [to] another, honoring the provided set of navigation [options].
     */
    fun route(from: P, to: P, options: O): Route<P>

    /**
     * Creates a set of options from the provided string.
     */
    fun parseOptions(options: String): O
}
