/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import java.util.random.RandomGenerator

/**
 * Provides random generators for different simulation contexts.
 *
 * @param forScenario The random generator for the scenario context.
 * @param forSimulation The random generator for the simulation context.
 */
data class RandomGeneratorProvider(val forScenario: RandomGenerator, val forSimulation: RandomGenerator)
