/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JvmName("RandomGeneratorExtension")

package it.unibo.alchemist.model

import org.apache.commons.math3.random.RandomGenerator

/**
 * Generates a random value in 0..2π.
 *
 */
fun RandomGenerator.randomAngle() = 2 * Math.PI * nextDouble()
