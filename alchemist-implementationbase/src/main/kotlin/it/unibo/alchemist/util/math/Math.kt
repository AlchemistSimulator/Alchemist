/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
@file:JvmName("Math")

package it.unibo.alchemist.util.math

import java.lang.Math.E
import kotlin.math.pow

/**
 * [Logistic function](https://en.wikipedia.org/wiki/Logistic_function).
 *
 * @param sigma Steepness parameter of the logistic function.
 * @param tau Threshold parameter of the logistic function.
 */
fun logistic(sigma: Double, tau: Double, vararg parameters: Double) =
    1 / (1 + E.pow(-sigma * (parameters.sum() - tau)))

/**
 *
 * @param sigma Steepness parameter of the advanced logistic function.
 * @param tau Threshold parameter of the advanced logistic function.
 */
fun advancedLogistic(sigma: Double, tau: Double, vararg parameters: Double) =
    logistic(sigma, tau, *parameters) - 1 / (1 + E.pow(sigma * tau)) * (1 + E.pow(-sigma * tau))
