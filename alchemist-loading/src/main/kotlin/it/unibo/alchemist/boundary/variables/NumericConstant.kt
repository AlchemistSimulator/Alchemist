/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.variables

import it.unibo.alchemist.boundary.DependentVariable
import java.io.Serial

/**
 * A numeric constant variable that always evaluates to the same [value].
 *
 * This class is a lightweight implementation of [DependentVariable] that
 * represents a fixed numeric value. Nullability has been removed: a
 * numeric constant must hold a non-null [Number].
 *
 * @param value the numeric value returned by this variable
 */
data class NumericConstant(val value: Number) : DependentVariable<Number> {
    override fun getWith(variables: Map<String, Any>): Number = value

    private companion object {
        @Serial
        private const val serialVersionUID = 1L
    }
}
