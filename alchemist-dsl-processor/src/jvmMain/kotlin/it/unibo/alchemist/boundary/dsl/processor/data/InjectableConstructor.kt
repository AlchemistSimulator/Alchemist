/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import it.unibo.alchemist.boundary.dsl.processor.extensions.isInjectable

internal data class InjectableConstructor(
    val constructor: KSFunctionDeclaration,
    val injectableParameters: List<KSValueParameter>,
    val preservedParameters: List<KSValueParameter>,
) {
    companion object {
        context(resolver: Resolver)
        operator fun invoke(constructor: KSFunctionDeclaration): InjectableConstructor? {
            val (injectable, preserved) = constructor.parameters.partition { it.type.resolve().isInjectable() }
            return when {
                injectable.isNotEmpty() && injectable.toSet().size == injectable.size && injectable.none {
                    it.isVararg
                } ->
                    InjectableConstructor(constructor, injectable, preserved)
                else -> null
            }
        }
    }
}
