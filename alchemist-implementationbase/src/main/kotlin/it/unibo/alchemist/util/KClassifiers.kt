/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier

/**
 * Utilities that extend the functionality of [KClassifier].
 */
object KClassifiers {
    /**
     * This classifier as a [KClass], or `null` if it is `null` or not a class (e.g., a type parameter).
     */
    fun KClassifier?.asKClass(): KClass<*>? = this as? KClass<*>

    /**
     * This classifier as a [KClass], or `null` if it is `null`, not a class, or [Any].
     */
    fun KClassifier?.asNonAnyKClass(): KClass<*>? = asKClass().takeIf { it != Any::class }
}
