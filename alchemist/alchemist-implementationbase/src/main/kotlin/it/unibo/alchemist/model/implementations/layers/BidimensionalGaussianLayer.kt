/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.implementations.layers

import it.unibo.alchemist.model.implementations.utils.BidimensionalGaussian
import it.unibo.alchemist.model.interfaces.Layer
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * A [Layer] based on a [2D gaussian function][BidimensionalGaussian] and an optional baseline value.
 */
open class BidimensionalGaussianLayer @JvmOverloads constructor(
    private val baseline: Double = 0.0,
    centerX: Double,
    centerY: Double,
    norm: Double,
    sigmaX: Double,
    sigmaY: Double = sigmaX
) : Layer<Double, Position2D<*>> {

    /**
     * The function on which the layer is based.
     */
    val function = BidimensionalGaussian(norm, centerX, centerY, sigmaX, sigmaY)

    override fun getValue(p: Position2D<*>) = baseline + function.value(p.x, p.y)
}