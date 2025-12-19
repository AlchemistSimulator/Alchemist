/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.extensions

import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Variance
import it.unibo.alchemist.boundary.dsl.processor.TypeExtractor
import it.unibo.alchemist.boundary.dsl.processor.replaceClassTypeParamReferences

internal fun KSTypeArgument.format(typeParameterNames: List<String>): String = when {
    type == null -> "*"
    variance == Variance.STAR -> "*"
    variance == Variance.CONTRAVARIANT -> transformWithVariance(typeParameterNames, "in")
    variance == Variance.COVARIANT -> transformWithVariance(typeParameterNames, "out")
    else -> transformWithVariance(typeParameterNames)
}

private fun KSTypeArgument.transformWithVariance(typeParameterNames: List<String>, variance: String? = null): String =
    type?.let {
        val typeStr = TypeExtractor.extractTypeString(it, emptyList())
        sequenceOf(variance, replaceClassTypeParamReferences(typeStr, typeParameterNames))
            .filterNotNull()
            .joinToString(" ")
    } ?: "*"
