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
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * Relative precision value under which two double values are considered to
 * be equal by fuzzyEquals.
 */
private const val DOUBLE_EQUALITY_EPSILON = 10e-12

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

/**
 * @param reference the value
 * @param v1 first value to compare to
 * @param v2 second value to compare to
 * @return v1 if val is closer to v1 than to v2, v2 otherwise
 */
fun closestTo(reference: Double, v1: Double, v2: Double): Double =
    if (abs(v1 - reference) < abs(v2 - reference)) v1 else v2

/**
 * Compares two double values, taking care of computing a relative error
 * tolerance threshold.
 *
 * @param a
 * first double
 * @param b
 * second double
 * @return true if the double are equals with a precision order of
 * DOUBLE_EQUALITY_EPSILON
 */
fun fuzzyEquals(a: Double, b: Double): Boolean = abs(a - b) <= DOUBLE_EQUALITY_EPSILON * max(abs(a), abs(b))

/**
 * Compares two double values, taking care of computing a relative error
 * tolerance threshold.
 *
 * @param a
 * first double
 * @param b
 * second double
 * @return true if a >= b, or if fuzzyEquals(a, b).
 */
fun fuzzyGreaterEquals(a: Double, b: Double): Boolean = a >= b || fuzzyEquals(a, b)

/**
 * Compares two double values, taking care of computing a relative error
 * tolerance threshold.
 *
 * @param a
 * first double
 * @param b
 * second double
 * @return true if a <= b, or if fuzzyEquals(a, b).
 */
fun fuzzySmallerEquals(a: Double, b: Double): Boolean = a <= b || fuzzyEquals(a, b)
