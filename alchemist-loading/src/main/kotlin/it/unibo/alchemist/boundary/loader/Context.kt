/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader

import it.unibo.alchemist.boundary.loader.LoadingSystemLogger.logger
import java.util.Collections.unmodifiableMap
import org.danilopianini.jirf.Factory

internal data class Context(
    private val namedLookup: MutableMap<String, Map<*, *>>,
    private val elementLookup: MutableMap<Map<*, *>, Any?> = mutableMapOf(),
    val factory: Factory = ObjectFactory.makeBaseFactory(),
) {
    private val backingConstants: MutableMap<String, Any?> = mutableMapOf()
    private val fixedVariables = mutableSetOf<String>()

    val constants: Map<String, Any?> get() = unmodifiableMap(backingConstants)

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

    /**
     * Registers a constant that has no YAML representation to be replaced in the model,
     * such as a scalar sitting directly under the variables section.
     */
    fun registerConstant(name: String, value: Any?) {
        logger.debug("Injecting constant {} with value {}", name, value)
        require(!constants.containsKey(name) || constants[name] == value) {
            "Inconsistent definition of constant named $name: previous value ${constants[name]}, current value $value"
        }
        namedLookup[name] = YAMLScalar
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
        if (key != YAMLScalar) {
            elementLookup[key] = value
        }
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

    private data object YAMLScalar : Map<Any, Any> by emptyMap()
}
