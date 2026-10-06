/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.observation.Observable
import kotlin.math.hypot

/**
 * A [Layer] representing a linear distribution in space of a molecule, which never changes over time.
 *
 * @param P [Position2D] type
 */
class BiomolGradientLayer<P : Position2D<P>>(
    directionX: Double,
    directionY: Double,
    unitVariation: Double,
    offset: Double,
) : Layer<Double, P> {
    private val a: Double
    private val b: Double
    private val c: Double = offset

    /**
     * The steepness of the gradient.
     */
    val steep: Double = unitVariation

    /**
     * Builds a gradient layer which grows in concentration proportionally in space.
     *
     * @param direction the [Position2D] representing, as a vector, the direction in which the gradient grows
     * @param unitVariation unit variation of the gradient
     * @param offset minimum value of concentration reached by this spatial distribution
     */
    constructor(direction: P, unitVariation: Double, offset: Double) :
        this(direction.x, direction.y, unitVariation, offset)

    init {
        val dirModule = hypot(directionX, directionY)
        assert(dirModule != 0.0)
        a = unitVariation * directionX / dirModule
        b = unitVariation * directionY / dirModule
    }

    /**
     * The parameters of the plane describing this spatial distribution:
     * concentration = `parameters[0]` * x + `parameters[1]` * y + `parameters[2]`.
     */
    val parameters: DoubleArray get() = doubleArrayOf(a, b, c)

    override fun getValue(p: P): Double = p.x * a + p.y * b + c

    override fun observeValue(position: P): Observable<Double> = Layer.constant(getValue(position))

    override fun toString(): String = "Layer representing a gradient of the molecule. " +
        "The equation describing this gradient is: concentration = ${a}x + ${b}y + $c"
}
