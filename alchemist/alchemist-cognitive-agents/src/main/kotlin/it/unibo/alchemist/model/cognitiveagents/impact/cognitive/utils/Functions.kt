/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitiveagents.impact.cognitive.utils

import kotlin.math.pow

/**
 * https://en.wikipedia.org/wiki/Logistic_function
 *
 * @param sigma Steepness parameter of the logistic function.
 * @param tau Threshold parameter of the logistic function.
 */
fun logistic(sigma: Double, tau: Double, vararg parameters: Double) =
    1 / (1 + Math.E.pow(-sigma * (parameters.sum() - tau)))

/**
 *
 * @param sigma Steepness parameter of the advanced logistic function.
 * @param tau Threshold parameter of the advanced logistic function.
 */
fun advancedLogistic(sigma: Double, tau: Double, vararg parameters: Double) =
    logistic(
        sigma,
        tau,
        *parameters
    ) - 1 / (1 + Math.E.pow(sigma * tau)) * (1 + Math.E.pow(-sigma * tau))
