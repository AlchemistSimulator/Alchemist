/*******************************************************************************
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 ******************************************************************************/
package it.unibo.alchemist.loader.displacements

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import org.apache.commons.math3.random.RandomGenerator
import java.util.stream.Stream
import java.util.stream.StreamSupport

/**
 * A (possibly randomized) grid of nodes.
 *
 * @param environment
 *            the {@link Environment}
 * @param randomGenerator
 *            the {@link RandomGenerator}
 * @param xStart
 *            the start x position
 * @param yStart
 *            the start y position
 * @param xEnd
 *            the end x position
 * @param yEnd
 *            the end y position
 * @param xStep
 *            how distant on the x axis (on average) nodes should be
 * @param yStep
 *            how distant on the y axis (on average) nodes should be
 * @param xRand
 *            how randomized should be positions along the x axis
 * @param yRand
 *            how randomized should be positions along the y axis
 * @param xShift
 *            how shifted should be positions between lines
 * @param yShift
 *            how shifted should be positions along columns
 */
open class Grid @JvmOverloads constructor(
    private val environment: Environment<*, *>,
    private val randomGenerator: RandomGenerator,
    private val xStart: Double,
    private val yStart: Double,
    private val xEnd: Double,
    private val yEnd: Double,
    private val xStep: Double,
    private val yStep: Double,
    private val xRand: Double = 0.0,
    private val yRand: Double = 0.0,
    private val xShift: Double = 0.0,
    private val yShift: Double = 0.0
) : Displacement<Position<*>> {

    override fun stream(): Stream<Position<*>> {
        val positions = (1 until stepCount(yStart, yEnd, yStep)).map { yn ->
            val y = yStart + yStep * yn
            (1 until stepCount(xStart, xEnd, xStep)).map { xn ->
                val x = xStart + xStep * xn
                val dx = xRand * (randomGenerator.nextDouble() - 0.5) + yn * xShift % xStep
                val dy = yRand * (randomGenerator.nextDouble() - 0.5) + xn * yShift % yStep
                environment.makePosition(x + dx, y + dy)
            }
        }.flatten()
        return StreamSupport.stream(positions.spliterator(), false)
    }

    private fun stepCount(min: Double, max: Double, step: Double): Int =
        Math.ceil(Math.abs((max - min) / step)).toInt().let {
            it + if (step * it <= Math.abs(max - min)) 1 else 0
        }
}
