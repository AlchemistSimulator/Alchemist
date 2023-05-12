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
import org.apache.commons.math3.distribution.MixtureMultivariateNormalDistribution
import org.apache.commons.math3.distribution.MultivariateNormalDistribution
import org.apache.commons.math3.random.RandomGenerator
import org.apache.commons.math3.util.Pair
import java.util.stream.Stream

/**
 * A generic [Deployment] that displaces a certain [nodeCount] of nodes in the proximity of a number of [sources].
 * Higher [variance] implies higher dispersion.
 * Subclasses must identify the [sources]
 */
abstract class AbstractCloseTo<T, P : Position<P>> constructor(
    protected val randomGenerator: RandomGenerator,
    protected val environment: Environment<T, P>,
    protected val nodeCount: Int,
    protected val variance: Double,
) : Deployment<P> {

    init {
        require(nodeCount >= 0) { "The node count must be positive or zero: $nodeCount" }
        require(variance >= 0) { "The node count must be positive or zero: $nodeCount" }
    }

    private var deployment: Collection<P>? = null

    protected open fun covarianceMatrix(dimensions: Int): Array<out DoubleArray> = Array(dimensions) { index ->
        DoubleArray(dimensions) { if (it == index) variance else 0.0 }
    }

    protected abstract val sources: Sequence<DoubleArray>

    final override fun stream(): Stream<P> = (
        deployment ?: sources
            .map { MultivariateNormalDistribution(randomGenerator, it, covarianceMatrix(it.size)) }
            .map { Pair(1.0, it) }
            .toList()
            .let { MixtureMultivariateNormalDistribution(randomGenerator, it) }
            .let { distribution ->
                (0 until nodeCount).map { environment.makePosition(*distribution.sample().toTypedArray()) }
            }.also { deployment = it }
        ).stream()
}
