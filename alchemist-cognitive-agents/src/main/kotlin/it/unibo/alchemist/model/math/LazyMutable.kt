/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.math

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A delegate allowing to lazily initialise a non-null mutable variable (= var).
 */
class LazyMutable<T>(private val initializer: () -> T) : ReadWriteProperty<Any?, T> {

    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value == null) {
            value = initializer()
        }
        return requireNotNull(value)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}

/**
 * Creates an instance of [LazyMutable] with the given [initializer].
 */
fun <T> lazyMutable(initializer: () -> T): LazyMutable<T> =
    LazyMutable(initializer)
