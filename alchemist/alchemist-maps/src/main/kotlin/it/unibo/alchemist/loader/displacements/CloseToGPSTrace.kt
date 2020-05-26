/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.displacements

import it.unibo.alchemist.boundary.gpsload.impl.TraceLoader
import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GeoPosition
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.random.RandomGenerator

/**
 * This [Displacement] displaces nodes in the proximity of a GPS trace.
 * Given a time interval [from] some time [to] another,
 * it creates a [TraceLoader], then uses the points in the interval to generate the sources for a
 * Gaussian bivariate function and uses its probability density to deploy.
 * Higher [variance] spreads nodes farther away from the trace with higher probability.
 */
class CloseToGPSTrace<T> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    environment: Environment<T, GeoPosition>,
    nodeCount: Int,
    variance: Double,
    private val from: Time = Time.ZERO,
    private val interval: Time = DoubleTime(1.0),
    val to: Time = Time.INFINITY,
    gpsFilePath: String,
    normalizerClass: String,
    vararg normalizerArguments: Any
) : AbstractCloseTo<T, GeoPosition>(randomGenerator, environment, nodeCount, variance) {

    private val traces = TraceLoader(gpsFilePath, normalizerClass, *normalizerArguments)
    override val sources = traces.asSequence()
        .flatMap { trace ->
            generateSequence(from) { it + interval }
                .takeWhile { it <= to }
                .map { trace.interpolate(it) }
                .map { doubleArrayOf(it.latitude, it.longitude) }
        }
}
