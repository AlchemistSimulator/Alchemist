/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader.util

import it.unibo.alchemist.util.ClassPathScanner
import java.lang.reflect.Modifier

internal data class TypeSearch<out T>(val typeName: String, val targetType: Class<out T>) {
    private val packageName: String? = typeName.substringBeforeLast('.', "").takeIf { it.isNotEmpty() }
    private val isQualified get() = packageName != null

    val subTypes: Collection<Class<out T>> by lazy {
        val compatibleTypes: List<Class<out T>> =
            when (packageName) {
                null ->
                    when {
                        targetType.packageName.startsWith("it.unibo.alchemist") ->
                            ClassPathScanner.subTypesOf(targetType, "it.unibo.alchemist")
                        else -> ClassPathScanner.subTypesOf(targetType)
                    }
                else -> ClassPathScanner.subTypesOf(targetType, packageName)
            }
        when {
            // The target type cannot be instanced, just return its concrete subclasses
            Modifier.isAbstract(targetType.modifiers) -> compatibleTypes
            // The target type can be instanced, return it and all its concrete subclasses
            else -> mutableSetOf(targetType).apply { addAll(compatibleTypes) }
        }
    }

    val perfectMatches: List<Class<out T>> by lazy {
        subtypes(ignoreCase = false)
    }

    val subOptimalMatches: List<Class<out T>> by lazy {
        subtypes(ignoreCase = true)
    }

    private fun subtypes(ignoreCase: Boolean) =
        subTypes.filter { typeName.equals(if (isQualified) it.name else it.simpleName, ignoreCase = ignoreCase) }

    companion object {
        inline fun <reified T> typeNamed(name: String) = TypeSearch(name, T::class.java)
    }
}
