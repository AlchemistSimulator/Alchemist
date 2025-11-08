/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.Variable
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Context for managing variables in a simulation.
 */
class VariablesContext {
    /**
     * Map of variable references.
     */
    val references: MutableMap<String, Any> = mutableMapOf()

    /**
     * Map of registered variables.
     */
    val variables: MutableMap<String, Variable<*>> = mutableMapOf()

    /**
     * Map of dependent variables.
     */
    val dependentVariables: MutableMap<String, () -> Any> = mutableMapOf()

    /**
     * Registers a variable provider.
     *
     * @param source The variable source.
     * @return A variable provider.
     */
    fun <T : Serializable> register(source: Variable<out T>): VariableProvider<T> = VariableProvider(source)

    /**
     * Registers a dependent variable provider.
     *
     * @param source The dependent variable source function.
     * @return A dependent variable provider.
     */
    fun <T : Serializable> dependent(source: () -> T): DependentVariableProvider<T> = DependentVariableProvider(source)

    /**
     * Provider for dependent variables that are computed from a source function.
     *
     * @param T The type of the variable value.
     * @param source The function that provides the variable value.
     */
    inner class DependentVariableProvider<T : Serializable>(private val source: () -> T) {
        /**
         * Provides a delegate for property delegation.
         *
         * @param thisRef The receiver object.
         * @param prop The property metadata.
         * @return A read-only property delegate.
         */
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> {
            check(!variables.containsKey(prop.name) && !dependentVariables.contains(prop.name)) {
                "Variable ${prop.name} already exists"
            }
            dependentVariables.put(prop.name, source)
            return DependentRef(source)
        }
    }

    /**
     * Read-only property delegate for dependent variables.
     *
     * @param T The type of the variable value.
     * @param source The function that provides the variable value.
     */
    inner class DependentRef<T : Serializable>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = source()

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
            error("Not allowed to assign a value to the variable ${property.name}")
    }

    /**
     * Provider for variables that are registered with a Variable source.
     *
     * @param T The type of the variable value.
     * @param source The variable source.
     */
    inner class VariableProvider<T : Serializable>(private val source: Variable<*>) {
        /**
         * Provides a delegate for property delegation.
         *
         * @param thisRef The receiver object.
         * @param prop The property metadata.
         * @return A read-only property delegate.
         */
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> {
            check(!variables.containsKey(prop.name) && !dependentVariables.contains(prop.name)) {
                "Variable ${prop.name} already exists"
            }
            variables[prop.name] = source
            references[prop.name] = source.default
            return Ref()
        }
    }

    /**
     * Read-write property delegate for variables.
     *
     * @param T The type of the variable value.
     */
    inner class Ref<T : Serializable> : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            check(references.contains(property.name)) {
                "Variable ${property.name} has no defined value"
            }
            @Suppress("UNCHECKED_CAST")
            return references[property.name] as T
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
            error("Not allowed to assign a value to the variable ${property.name}")
    }
}
