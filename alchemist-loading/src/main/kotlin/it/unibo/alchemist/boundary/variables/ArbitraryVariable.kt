/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.variables

/**
 * A variable that can take any value from an arbitrary, finite set.
 *
 * This implementation stores values in an immutable ordered set. Values and
 * the default are non-nullable: an [ArbitraryVariable] always contains at
 * least a default value and a (possibly empty) set of candidate values.
 *
 * @property values the set of allowed values (non-nullable)
 * @property default the default value (non-nullable)
 */
data class ArbitraryVariable(override val default: Any, private val values: List<Any?>) :
    AbstractPrintableVariable<Any?>() {

    init {
        require(values.distinct() == values) {
            "Values must be distinct: found duplicates in $values"
        }
    }

    /**
     * Convenience constructor: build from a default value and a vararg of doubles.
     * @param default the default value
     * @param values values to include in the set
     */
    constructor(default: Any, vararg values: Double) : this(
        default,
        values.toList(),
    )

    /**
     * Construct from a default value and any iterable collection of values.
     * @param default the default value
     * @param values iterable containing the allowed values
     */
    constructor(default: Any, values: Iterable<Any?>) : this(default, values.toList())

    override fun stream() = values.stream()
}
