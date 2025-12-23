/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import it.unibo.alchemist.boundary.dsl.processor.extensions.toStringWithGenerics

/**
 * Processes type bounds for type parameters, cleaning up internal Kotlin type representations.
 */
internal object TypeBoundProcessor {
    /**
     * Processes a type bound reference, cleaning up internal Kotlin type representations and variance annotations.
     *
     * @param bound The type reference to process
     * @param classTypeParamNames List of class type parameter names to replace in the bound
     * @return A cleaned string representation of the bound with fully qualified names
     */
    fun processBound(bound: KSTypeReference, classTypeParamNames: List<String> = emptyList()): String {
        val resolved = bound.resolve()
        val decl = resolved.declaration
        val qualifiedName = decl.qualifiedName?.asString()
        if (qualifiedName != null) {
            val result = resolved.toStringWithGenerics(classTypeParamNames)
            return replaceClassTypeParamReferences(result, classTypeParamNames)
        }
        val result = TypeExtractor.extractTypeString(bound, emptyList())
        return replaceClassTypeParamReferences(result, classTypeParamNames)
    }
}

// TODO: revise this function
internal fun replaceClassTypeParamReferences(boundStr: String, classTypeParamNames: List<String>): String {
    // Strip redundant qualification when the matcher references
    // the same class-level type parameter
    if (classTypeParamNames.isEmpty()) {
        return boundStr
    }
    var result = boundStr
    classTypeParamNames.forEach { paramName ->
        val pattern = Regex("""\b[\w.]+\.$paramName\b""")
        result = pattern.replace(result) { matchResult ->
            val matched = matchResult.value
            val prefix = matched.substringBefore(".$paramName")
            if (prefix.contains(".")) {
                paramName
            } else {
                matched
            }
        }
    }
    return result
}
