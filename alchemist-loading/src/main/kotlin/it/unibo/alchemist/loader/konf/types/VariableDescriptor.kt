/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.konf.types

import arrow.core.Either
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import it.unibo.alchemist.loader.konf.requireAllNulls
import it.unibo.alchemist.loader.variables.DependentVariable
import it.unibo.alchemist.loader.variables.JSR223Variable
import it.unibo.alchemist.loader.variables.LinearVariable
import it.unibo.alchemist.loader.variables.Variable
import org.danilopianini.jirf.Factory
import java.io.Serializable
import java.lang.IllegalArgumentException

sealed class VariableDescriptor<out V : Serializable> {

    abstract fun build(factory: Factory): Either<Variable<out V>, DependentVariable<out V>>

    companion object {
        @JsonCreator
        @JvmStatic
        fun <V : Serializable> create(
            @JsonProperty("min") min: Any,
            @JsonProperty("max") max: Any,
            @JsonProperty("step") step: Any,
            @JsonProperty("default") default: Any,
        ): VariableDescriptor<V> {
            listOf<Any>(min, max, step, default)
            TODO()
        }

        @JsonCreator
        @JvmStatic
        fun <V : Serializable> create(
            @JsonProperty("type") type: Any?,
            @JsonProperty("parameters") parameters: Iterable<*>?,
            @JsonProperty("min") min: Number?,
            @JsonProperty("max") max: Number?,
            @JsonProperty("step") step: Number?,
            @JsonProperty("default") default: Number?,
            @JsonProperty("formula") formula: Any?,
            @JsonProperty("language") language: Any?,
        ): VariableDescriptor<V> {
            return when {
                type != null -> {
                    requireAllNulls(min, max, step, default, formula, language)
                    ArbitraryTypeVariable(JVMConstructor.create(type, parameters))
                }
                min != null || max != null || default != null || step != null-> {
                    requireAllNulls(type, parameters, formula, language)
                    requireNotNull(min) { "Unspecified min value for linear variable" }
                    requireNotNull(max) { "Unspecified max value for linear variable" }
                    requireNotNull(step) { "Unspecified step value for linear variable" }
                    requireNotNull(default) { "Unspecified default value for linear variable" }
                    @Suppress("UNCHECKED_CAST")
                    LinearVariableDescriptor(default, min, max, step) as VariableDescriptor<V>
                }
                formula != null -> {
                    requireAllNulls(type, parameters, min, max, step, default)
//                    JSR223VariableDescriptor(formula, language ?: "groovy")
                    TODO()
                }
                else -> throw IllegalArgumentException(
                    """
                        Variables must be one of:
                          - Arbitrary type / parameters syntax
                          - A linear variable with default / min / max / step parameters
                          - A formula in a valid JSR223 language (defaults to Groovy), with the formula / language pair 
                    """.trimIndent()
                )
            }
        }
    }
}

class ArbitraryTypeVariable<out V : Serializable>(val constructor: JVMConstructor) : VariableDescriptor<V>() {
    override fun build(factory: Factory) = Either.left(constructor.buildAny<Variable<out V>>(factory))
}

class LinearVariableDescriptor(val default: Number, val min: Number, val max: Number, val step: Number) : VariableDescriptor<Double>() {
    override fun build(factory: Factory) =
        Either.left(LinearVariable(default.toDouble(), min.toDouble(), max.toDouble(), step.toDouble()))
}

class JSR223VariableDescriptor<V : Serializable>(val formula: () -> String, val language: () -> String = { "groovy" }) : VariableDescriptor<V>() {
    override fun build(factory: Factory) = Either.right(JSR223Variable<V>(formula(), language()))
}

