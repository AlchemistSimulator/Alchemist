/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.util.math

import org.apache.commons.math3.analysis.BivariateFunction
import org.apache.commons.math3.util.FastMath.exp
import java.io.Serializable

/**
 * A 2D gaussian function.
 */
class BidimensionalGaussian(
    private val amplitude: Double,
    private val x0: Double,
    private val y0: Double,
    private val sigmaX: Double,
    private val sigmaY: Double,
) : BivariateFunction, Serializable {

    override fun value(x: Double, y: Double): Double {
        val dx = x - x0
        val dy = y - y0
        val sigmaXsq = 2 * sigmaX * sigmaX
        val sigmaYsq = 2 * sigmaY * sigmaY
        return amplitude * exp(-(dx * dx / sigmaXsq + dy * dy / sigmaYsq))
    }

    /**
     * The integral of the function.
     *
     * @return The computed value of the integral.
     */
    fun integral() = 2 * Math.PI * amplitude * sigmaX * sigmaY

    companion object {
        private const val serialVersionUID = 1L
    }
}
