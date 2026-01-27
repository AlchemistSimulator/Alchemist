/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import org.apache.commons.math3.random.RandomGenerator

/**
 * Context for defining random seeds for simulation and scenario generation.
 */
interface SeedsContext {

    /**
     * Sets the scenario's random generator to the default with the specified seed.
     *
     * @param seed the seed value
     */
    fun scenario(seed: Long)

    /**
     * Sets the scenario's random generator using the provided block.
     *
     * @param block a lambda that returns a RandomGenerator
     */
    fun scenario(block: () -> RandomGenerator)

    /**
     * Sets the simulation's random generator to the default with the specified seed.
     *
     * @param seed the seed value
     */
    fun simulation(seed: Long)

    /**
     * Sets the simulation's random generator using the provided block.
     *
     * @param block a lambda that returns a RandomGenerator
     */
    fun simulation(block: () -> RandomGenerator)
}
