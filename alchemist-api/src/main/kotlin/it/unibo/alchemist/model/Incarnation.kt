/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model

import org.apache.commons.math3.random.RandomGenerator

/**
 * An incarnation: the factory of the model entities of a specific computational model.
 *
 * @param T concentration type
 * @param P position type
 */
interface Incarnation<T, P : Position<out P>> {
    /**
     * Creates an [Action] hosted by [reaction], and by [node] (if any), in [environment],
     * using [randomGenerator] and parsing [additionalParameters].
     */
    fun createAction(
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>?,
        reaction: Reaction<T>,
        additionalParameters: Any?,
    ): Action<T>

    /**
     * Creates a new concentration of a specific concrete type, parsing [descriptor].
     */
    fun createConcentration(descriptor: Any? = null): T

    /**
     * Creates a [Condition] hosted by [reaction], and by [node] (if any), in [environment],
     * using [randomGenerator] and parsing [additionalParameters].
     */
    fun createCondition(
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>?,
        reaction: Reaction<T>,
        additionalParameters: Any?,
    ): Condition<T>

    /**
     * Parses [s] into a [Molecule].
     */
    fun createMolecule(s: String): Molecule

    /**
     * Creates a [Node] for [environment], using [randomGenerator] and parsing [parameter].
     */
    fun createNode(randomGenerator: RandomGenerator, environment: Environment<T, P>, parameter: Any?): Node<T>

    /**
     * Creates an environment-level [Reaction] in [environment], using [randomGenerator] and parsing [parameter].
     */
    fun createReaction(randomGenerator: RandomGenerator, environment: Environment<T, P>, parameter: Any?): Reaction<T>

    /**
     * Creates a [NodeReaction] hosted by [node] in [environment], scheduled by [timeDistribution],
     * using [randomGenerator] and parsing [parameter].
     */
    fun createNodeReaction(
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>,
        timeDistribution: TimeDistribution<T>,
        parameter: Any?,
    ): NodeReaction<T>

    /**
     * Creates a [TimeDistribution] for a reaction hosted by [node] (if any) in [environment],
     * using [randomGenerator] and parsing [parameter].
     */
    fun createTimeDistribution(
        randomGenerator: RandomGenerator,
        environment: Environment<T, P>,
        node: Node<T>?,
        parameter: Any?,
    ): TimeDistribution<T>

    /**
     * Extracts a numeric value from [node], given a [molecule] and a [property].
     * Returns [Double.NaN] if a numeric value is not deducible.
     */
    fun readConcentration(node: Node<T>, molecule: Molecule, property: String): Double
}
