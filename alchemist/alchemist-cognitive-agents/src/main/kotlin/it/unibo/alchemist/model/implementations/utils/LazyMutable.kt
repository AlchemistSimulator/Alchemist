/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.utils

import java.util.Optional
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A delegate allowing to lazily initialise a mutable variable (= var).
 */
class LazyMutable<T>(private val initializer: () -> T) : ReadWriteProperty<Any?, T> {

    private var value: Optional<T> = Optional.empty()

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        if (value.isEmpty) {
            value = Optional.of(initializer())
        }
        return value.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = Optional.of(value)
    }
}

/**
 * Creates an instance of [LazyMutable] with the given [initializer].
 */
fun <T> lazyMutable(initializer: () -> T): LazyMutable<T> = LazyMutable(initializer)
