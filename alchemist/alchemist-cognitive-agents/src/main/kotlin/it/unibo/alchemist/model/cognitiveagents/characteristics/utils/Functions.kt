package it.unibo.alchemist.model.cognitiveagents.characteristics.utils

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
    logistic(sigma, tau, *parameters) - 1 / (1 + Math.E.pow(sigma*tau)) * (1 + Math.E.pow(-sigma*tau))