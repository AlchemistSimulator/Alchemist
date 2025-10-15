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

class VariablesContext {
    val references: MutableMap<String, Any> = mutableMapOf()
    val variables: MutableMap<String, Variable<*>> = mutableMapOf()
    val dependentVariables: MutableMap<String, () -> Any> = mutableMapOf()

    fun <T : Serializable> register(source: Variable<out T>): VariableProvider<T> = VariableProvider(source)
    fun <T : Serializable> dependent(source: () -> T): DependentVariableProvider<T> = DependentVariableProvider(source)

    inner class DependentVariableProvider<T : Serializable>(private val source: () -> T) {
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> {
            if (variables.containsKey(prop.name) || dependentVariables.contains(prop.name)) {
                throw IllegalStateException("Variable ${prop.name} already exists")
            }
            dependentVariables.put(prop.name, source)
            return DependentRef(source)
        }
    }
    inner class DependentRef<T : Serializable>(private val source: () -> T) : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = source()

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
            throw IllegalStateException("Not allowed to assign a value to the variable ${property.name}")
    }
    inner class VariableProvider<T : Serializable>(private val source: Variable<*>) {
        operator fun provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> {
            if (variables.containsKey(prop.name) || dependentVariables.contains(prop.name)) {
                throw IllegalStateException("Variable ${prop.name} already exists")
            }
            variables[prop.name] = source
            references[prop.name] = source.default
            return Ref()
        }
    }

    inner class Ref<T : Serializable> : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (!references.contains(property.name)) {
                throw IllegalStateException("Variable ${property.name} has no defined value")
            }
            @Suppress("UNCHECKED_CAST")
            return references[property.name] as T
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T): Unit =
            throw IllegalStateException("Not allowed to assign a value to the variable ${property.name}")
    }
}
