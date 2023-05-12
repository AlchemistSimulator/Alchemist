/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.layers

import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.util.math.BidimensionalGaussian

/**
 * A [Layer] based on a [2D gaussian function][BidimensionalGaussian] and an optional baseline value.
 *
 * @param centerX x coord of the layer's center.
 * @param centerY y coord of the layer's center.
 */
open class BidimensionalGaussianLayer<P : Position2D<P>> @JvmOverloads constructor(
    private val baseline: Double = 0.0,
    val centerX: Double,
    val centerY: Double,
    norm: Double,
    sigmaX: Double,
    sigmaY: Double = sigmaX,
) : Layer<Double, P> {

    /**
     * The function on which the layer is based.
     */
    val function =
        BidimensionalGaussian(norm, centerX, centerY, sigmaX, sigmaY)

    override fun getValue(p: P) = baseline + function.value(p.x, p.y)
}
