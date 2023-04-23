/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.timedistributions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.timedistributions.AnyRealDistribution
import it.unibo.alchemist.util.RealDistributionUtil
import org.apache.commons.math3.distribution.RealDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * A special TimeDistribution that schedules the reaction after [start],
 * according to the value of a [molecule] which contains the delta time.
 * If a [property] is specified, the value to be interpreted as time delta is read from the [incarnation].
 * Otherwise, the [node] is accessed directly for reading the value.
 *
 * It's possible to associate an [errorDistribution] to this time distribution, whose samples will be used
 * to shift the time samples.
 *
 * There are some conditions to be satisfied:
 * - [molecule] must be modified exclusively by the reaction being scheduled
 * - [molecule] must exist in the node. If it does not and the environment returns null, it is assumed to be zero
 * - [molecule] must have a positive or zero value associated.
 * - [molecule]'s concentration must have a type which is understandable as a positive number
 *   ([Number], [Time], or a parse-able [String]).
 * - the [errorDistribution]'s samples plus the value of the [molecule] concentration (or property value)
 *   **must** always be greater than zero. It is thus recommended to use an [errorDistribution] whose
 *   support lower bound is zero or greater
 */
class MoleculeControlledTimeDistribution<T> @JvmOverloads constructor(
    private val incarnation: Incarnation<T, *>,
    val node: Node<T>,
    val molecule: Molecule,
    val property: String? = null,
    val start: Time = Time.ZERO,
    val errorDistribution: RealDistribution? = null,
) : AnyRealDistribution<T>(
    start,
    object : RealDistribution {

        /*
         * Unknown values
         */
        override fun probability(x: Double) = TODO()
        override fun density(x: Double) = TODO()
        override fun cumulativeProbability(x: Double) = TODO()

        @Deprecated(message = "Deprecated in Apache Commons")
        override fun cumulativeProbability(x0: Double, x1: Double) = TODO()
        override fun inverseCumulativeProbability(p: Double) = TODO()
        override fun getNumericalVariance() = TODO()
        override fun isSupportConnected() = TODO()
        override fun reseedRandomGenerator(seed: Long) = TODO()

        /*
         * Known values
         */
        override fun getSupportLowerBound() = 0.0
        override fun getSupportUpperBound() = Double.MAX_VALUE

        @Deprecated(message = "Deprecated in Apache Commons")
        override fun isSupportLowerBoundInclusive() = true

        @Deprecated(message = "Deprecated in Apache Commons")
        override fun isSupportUpperBoundInclusive() = false

        /*
         * Computable stuff
         */
        override fun getNumericalMean() = currentValue + (errorDistribution?.numericalMean ?: 0.0)

        override fun sample() = currentValue + (errorDistribution?.sample() ?: 0.0)

        override fun sample(sampleSize: Int): DoubleArray = DoubleArray(sampleSize) { sample() }

        val currentValue get() = readCurrentValue(incarnation, node, molecule, property)
    },
) {

    @JvmOverloads constructor(
        incarnation: Incarnation<T, *>,
        randomGenerator: RandomGenerator,
        node: Node<T>,
        molecule: Molecule,
        property: String? = null,
        start: Time = Time.ZERO,
        distributionName: String,
        vararg distributionParametrs: Double,
    ) : this(
        incarnation,
        node,
        molecule,
        property,
        start,
        RealDistributionUtil.makeRealDistribution(randomGenerator, distributionName, *distributionParametrs),
    )

    constructor(
        incarnation: Incarnation<T, *>,
        randomGenerator: RandomGenerator,
        node: Node<T>,
        molecule: Molecule,
        start: Time = Time.ZERO,
        distributionName: String,
        vararg distributionParametrs: Double,
    ) : this(
        incarnation = incarnation,
        randomGenerator = randomGenerator,
        node = node,
        molecule = molecule,
        property = null,
        start = start,
        distributionName = distributionName,
        distributionParametrs = distributionParametrs,
    )

    private var previousStep: Double? = null

    override fun updateStatus(currentTime: Time, executed: Boolean, param: Double, environment: Environment<T, *>) {
        val currentStep = readCurrentValue(incarnation, node, molecule, property)
        if (executed) {
            previousStep = currentStep
        } else {
            require(currentStep == previousStep) {
                "Something nasty happened: molecule $molecule is being used as a scheduler, but " +
                    "some reaction other than the one using it for scheduling changed the concentration. " +
                    "This is unsupported and sends the simulator into an inconsistent state, " +
                    "hence the simulation has been forcibly terminated."
            }
        }
        super.updateStatus(currentTime, executed, param, environment)
    }

    override fun cloneOnNewNode(destination: Node<T>, currentTime: Time?): MoleculeControlledTimeDistribution<T> =
        MoleculeControlledTimeDistribution(incarnation, destination, molecule, property, start, errorDistribution)

    companion object {
        private fun <T> readCurrentValue(
            incarnation: Incarnation<T, *>,
            node: Node<T>,
            molecule: Molecule,
            property: String?,
        ): Double {
            val currentValue = if (property != null) {
                incarnation.getProperty(node, molecule, property)
            } else {
                when (val value = node.getConcentration(molecule)) {
                    is Number -> value.toDouble()
                    is String -> value.toDouble()
                    is Time -> value.toDouble()
                    null -> 0.0
                    else -> error(
                        "Expected a numeric value in $molecule at node ${node.id}, " +
                            "but '$value' of type '${value.let { it::class.simpleName }}' was found instead",
                    )
                }
            }
            require(currentValue >= 0) {
                "You requested to be scheduled with a delta of $currentValue in molecule $molecule at node ${node.id}" +
                    ". Alchemist loves causality and won't let you go back in time"
            }
            return currentValue
        }
    }
}
