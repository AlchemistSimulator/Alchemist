/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy.missing

/**
 * A [MissingValueStrategy] that replaces with a constant value.
 *
 * @param value the constant value to return.
 * @param T the type of the constant.
 */
class ConstantValue<T>(private val value: T?) : MissingValueStrategy<T> {
    override fun value(): T? = value
}

/**
 * A [MissingValueStrategy] that returns a constant [Double].
 */
class ConstantDoubleValue(replacement: Double) : MissingValueStrategy<Double> by ConstantValue(replacement)

/**
 * A [MissingValueStrategy] that propagates `null`.
 */
class PropagateNull<T> : MissingValueStrategy<T> {
    override fun value(): T? = null
}
