/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors listed in the main project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import org.apache.commons.math3.random.RandomGenerator
import java.util.stream.IntStream
import java.util.stream.Stream

/**
 * @param environment
 * the [Environment]
 * @param randomGenerator
 * the [RandomGenerator]
 * @param nodeCount
 * the number of nodes
 * @param <P>
 */
abstract class AbstractRandomDeployment<P : Position<out P>> (
    protected val environment: Environment<*, P>,
    protected val randomGenerator: RandomGenerator,
    protected val nodeCount: Int,
) : Deployment<P> {

    override fun stream(): Stream<P> = IntStream.range(0, nodeCount).mapToObj { this.indexToPosition(it) }

    /**
     * Builds a position, relying on the internal [Environment].
     *
     * @see Environment.makePosition
     * @param coordinates the coordinates
     * @return a position
     */
    protected fun makePosition(vararg coordinates: Number): P = environment.makePosition(*coordinates)

    /**
     * @param from
     * minimum value
     * @param to
     * maximum value
     * @return a random uniformly distributed in such range
     */
    protected fun randomDouble(from: Double, to: Double): Double =
        randomGenerator.nextDouble() * Math.abs(to - from) + Math.min(from, to)

    /**
     * @return a random double in the [0, 1] interval
     */
    protected fun randomDouble(): Double = randomGenerator.nextDouble()

    /**
     * @param from
     * minimum value
     * @param toExclusive
     * maximum value (exclusive)
     * @return a random uniformly distributed in such range
     */
    protected fun randomInt(from: Int, toExclusive: Int): Int =
        randomGenerator.nextInt(Math.abs(toExclusive - from)) + Math.min(from, toExclusive)

    /**
     * @param i
     * the node number
     * @return the position of the node
     */
    protected abstract fun indexToPosition(i: Int): P
}
