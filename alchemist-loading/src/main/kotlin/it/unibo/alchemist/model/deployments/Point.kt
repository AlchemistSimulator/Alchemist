/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import java.util.stream.Stream

/**
 * A single node in a single point.
 *
 * @param P position type
 * @param environment the [Environment]
 * @param x x coordinate
 * @param y y coordinate
 */
class Point<P : Position<out P>>(
    private val environment: Environment<*, P>,
    private val x: Double,
    private val y: Double,
) : Deployment<P> {
    /**
     * Builds a new deployment of a single point at integer coordinates ([x], [y]) of the [environment].
     */
    constructor(environment: Environment<*, P>, x: Int, y: Int) : this(environment, x.toDouble(), y.toDouble())

    /**
     * Builds a new deployment of a single point at coordinates ([x], [y]) of the [environment].
     */
    constructor(environment: Environment<*, P>, x: Double, y: Int) : this(environment, x, y.toDouble())

    /**
     * Builds a new deployment of a single point at coordinates ([x], [y]) of the [environment].
     */
    constructor(environment: Environment<*, P>, x: Int, y: Double) : this(environment, x.toDouble(), y)

    override fun stream(): Stream<P> = Stream.of(environment.makePosition(x, y))
}
