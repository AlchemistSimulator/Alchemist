/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.kotlindsl

import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Contract for DSL components that provide delegated, read-only variables.
 *
 * This interface integrates with Kotlin's delegated property mechanism by exposing [provideDelegate].
 * Implementations can intercept property delegation at declaration time to:
 * - register the variable under the chosen property name;
 * - return a [ReadOnlyProperty] that supplies the actual value at access time.
 *
 * The value type [V] must be [Serializable] to match Alchemist's requirements for scenario variables.
 *
 * @param V the type of value provided by the delegate.
 */
interface VariableProvider<V : Serializable> {

    /**
     * Intercepts the delegation of a property and returns the [ReadOnlyProperty] that will supply its value.
     *
     * Implementations typically use [property.name] as the variable identifier and may use [thisRef] to
     * associate the delegate with the owning instance, if needed.
     *
     * @param thisRef the object on which the property is delegated (or `null` for top-level properties).
     * @param property the metadata of the delegated property.
     * @return a read-only property delegate providing values of type [V].
     */
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadOnlyProperty<Any?, V>
}
