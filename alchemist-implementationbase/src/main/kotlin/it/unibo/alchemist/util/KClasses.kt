/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.util.KTypes.asNonAnyKClass
import kotlin.reflect.KClass

/**
 * Utilities that extend the functionality of [KClass].
 */
object KClasses {

    /**
     * The [simple name][KClass.simpleName] of this class or, for anonymous classes (e.g., object expressions and
     * lambdas), which have none, `<anonymous Name>`, where `Name` is the simple name of its closest named supertype
     * other than [Any] (`<anonymous object>` if there is none).
     */
    val KClass<*>.nonNullSimpleName: String get() = simpleName
        ?: "<anonymous ${allNonAnySuperKClasses().firstNotNullOfOrNull { it.simpleName } ?: "object"}>"

    /**
     * All the classes and interfaces this class extends or implements, directly or not, except [Any],
     * each listed once: first the direct ones, then, for each of them in turn, their own supertypes.
     * Supertypes whose classifier is not a class (e.g., type parameters) are skipped.
     */
    fun KClass<*>.allNonAnySuperKClasses(): Sequence<KClass<*>> {
        val directSuperclasses = supertypes.mapNotNull { it.asNonAnyKClass() }
        return (
            directSuperclasses.asSequence() +
                directSuperclasses.asSequence().flatMap { it.allNonAnySuperKClasses() }
            )
            .distinct()
    }
}
