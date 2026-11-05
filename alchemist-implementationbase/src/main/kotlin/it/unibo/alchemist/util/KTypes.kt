/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.util.KClasses.allNonAnySuperKClasses
import it.unibo.alchemist.util.KClassifiers.asKClass
import it.unibo.alchemist.util.KClassifiers.asNonAnyKClass
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Utilities that extend the functionality of [KType].
 */
object KTypes {

    /**
     * The class of this type, or `null` if this type is `null` or its classifier is not a class
     * (e.g., a type parameter).
     */
    fun KType?.asKClass(): KClass<*>? = this?.classifier.asKClass()

    /**
     * The class of this type, or `null` if this type is `null`, its classifier is not a class, or it is [Any].
     */
    fun KType?.asNonAnyKClass(): KClass<*>? = this?.classifier.asNonAnyKClass()

    /**
     * All the supertypes of the class of this type, except [Any], as in [KClass.allNonAnySuperKClasses];
     * none if the classifier of this type is not a class.
     */
    fun KType.allNonAnySuperKClasses(): Sequence<KClass<*>> = when (val classifier = classifier) {
        is KClass<*> -> classifier.allNonAnySuperKClasses()
        else -> emptySequence()
    }
}
