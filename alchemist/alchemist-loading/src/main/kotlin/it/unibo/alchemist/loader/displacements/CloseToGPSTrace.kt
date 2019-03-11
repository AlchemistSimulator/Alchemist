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

class CloseToGPSTrace<T> @JvmOverloads constructor(
    randomGenerator: RandomGenerator,
    environment: Environment<T, GeoPosition>,
    nodeCount: Int,
    variance: Double,
    private val from: Time = DoubleTime.ZERO_TIME,
    private val interval: Time = DoubleTime(1.0),
    val to: Time = DoubleTime.INFINITE_TIME,
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