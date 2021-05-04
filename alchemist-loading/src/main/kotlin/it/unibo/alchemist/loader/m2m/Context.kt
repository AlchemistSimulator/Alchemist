/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.m2m

import org.danilopianini.jirf.Factory
import it.unibo.alchemist.loader.m2m.LoadingSystemLogger.logger

internal data class Context constructor(
    private val namedLookup: MutableMap<String, Map<*, *>>,
    private val elementLookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
    val factory: Factory = ObjectFactory.makeBaseFactory(),
) {

    private var backingConstants: MutableMap<String, Any?> = mutableMapOf()
    private val fixedVariables = mutableSetOf<String>()

    val constants: Map<String, Any?> get() = backingConstants

    constructor() : this(namedLookup = mutableMapOf())

    fun child(): Context = Context(namedLookup = this.namedLookup)

    fun registerConstant(name: String, representation: Map<*, *>, value: Any?) {
        logger.debug("Injecting constant {} with value {} represented by {}", name, value, representation)
        if (constants.containsKey(name)) {
            val previous = elementLookup[representation]
            require(value == previous) {
                """
                    Inconsistent definition of constant named $name:
                      - previous evaluation: ${elementLookup[representation]}
                      - current value: $value
                    Item originating this issue: $representation
                    Context at time of failure: $this
                """.trimIndent()
            }
        }
        namedLookup[name] = representation
        elementLookup[representation] = value
        backingConstants[name] = value
    }

    fun registerVariable(name: String, representation: Map<*, *>) {
        logger.debug("Injecting variable {} represented by {}", name, representation)
        namedLookup[name] = representation
        elementLookup[representation] = SimulationModel.PlaceHolderForVariables(name)
    }

    fun fixVariableValue(name: String, value: Any?) {
        val key = requireNotNull(namedLookup[name]) {
            """
                There is no known "$name" object in the lookup table, although there should be by construction.
                Known object names: ${namedLookup.keys}
                This sounds like a bug in Alchemist.
            """.trimIndent()
        }
        elementLookup[key] = value
        fixedVariables += name
        logger.debug("Contextually set {} = {}. Currently fixed: {}.", name, value, fixedVariables)
    }

    /**
     * Returns null if the element is not a resolvable entity.
     * Othewise, returns a Result with the resolution result.
     */
    fun lookup(representation: Map<*, *>): Any? =
        if (elementLookup.containsKey(representation)) elementLookup[representation] else null

    /**
     * Returns null if the element is not a resolvable entity.
     * Othewise, returns a Result with the resolution result.
     */
    fun lookup(placeholder: SimulationModel.PlaceHolderForVariables): Any? =
        placeholder.takeUnless { placeholder.name in fixedVariables }
            ?: requireNotNull(namedLookup[placeholder.name]) {
                "Bug in Alchemist: unresolvable variable ${placeholder.name}"
            }.let { lookup(it) }
}
