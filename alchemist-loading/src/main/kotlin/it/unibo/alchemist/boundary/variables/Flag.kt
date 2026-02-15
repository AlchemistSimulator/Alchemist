/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.variables

import java.io.Serial
import java.util.stream.Stream

/**
 * A boolean flag variable. Charts typically accept numeric values, but the
 * semantic value of this variable is boolean; use `true` and `false` as the
 * two possible outputs. The default value is provided at construction.
 *
 * This is equivalent in behavior to a [LinearVariable] with two samples
 * between 0 and 1, but keeps the boolean semantics.
 *
 * @param default the default boolean value
 */
data class Flag(override val default: Boolean) : AbstractPrintableVariable<Boolean>() {

    override fun stream(): Stream<Boolean> = Stream.of(true, false)

    private companion object {
        @Serial
        private const val serialVersionUID = 1L
    }
}
