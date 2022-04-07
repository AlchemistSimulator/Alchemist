/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.util

import org.apache.commons.math3.random.RandomGenerator

/**
 * Utilities that extend the functionality of [RandomGenerator].
 */
object RandomGeneratorExtension {
    /**
     * Generate a random double between the given bounds.
     *
     * @param from
     *          the lower bound.
     * @param to
     *          the upper bound.
     */
    fun RandomGenerator.nextDouble(from: Double, to: Double) = nextDouble() * (to - from) + from
}
