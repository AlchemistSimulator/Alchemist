/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

@file:JvmName("TimeExtension")

package it.unibo.alchemist.model

import it.unibo.alchemist.model.implementations.times.DoubleTime
import it.unibo.alchemist.model.interfaces.Time

/**
 * Plus operator for [Time] and [Double].
 */
operator fun Time.plus(other: Double): Time = plus(DoubleTime(other))

/**
 * Minus operator for [Time] and [Double].
 */
operator fun Time.minus(other: Double): Time = minus(DoubleTime(other))
