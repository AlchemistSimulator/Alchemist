/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.datatypes

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

internal data class InjectableConstructor(
    val constructor: KSFunctionDeclaration,
    val injectableParameters: List<KSValueParameter>,
    val preservedParameters: List<KSValueParameter>,
) {
    val returnType: KSType? by lazy { constructor.returnType?.resolve() }
    val injectableTypes by lazy { injectableParameters.map { it.type.resolve() } }
    val preservedTypes by lazy { preservedParameters.map { it.type.resolve() } }

    override fun toString(): String = "InjectableConstructor(${returnType?.toString()}, " +
        "injectableParameters=${injectableParameters.typedAsString()}, " +
        "preservedParameters=${preservedParameters.typedAsString()}"

    private fun List<KSValueParameter>.typedAsString(): String = this.zip(this.map { it.type.resolve() })
        .joinToString(", ", prefix = "[", postfix = "]") { (param, type) -> "${param.name?.asString()}: $type" }

    override fun equals(other: Any?): Boolean = other is InjectableConstructor &&
        other.returnType == returnType &&
        other.injectableTypes == injectableTypes &&
        other.preservedTypes == preservedTypes

    override fun hashCode(): Int = listOf(
        returnType,
        injectableTypes,
        preservedTypes,
    ).hashCode()
}
