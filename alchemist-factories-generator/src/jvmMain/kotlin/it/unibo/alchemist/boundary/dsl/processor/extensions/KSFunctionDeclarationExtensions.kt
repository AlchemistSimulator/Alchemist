/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.extensions

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import it.unibo.alchemist.boundary.dsl.processor.datatypes.InjectableConstructor

/**
 * Finds a public constructor for the given class declaration.
 * Prefers the primary constructor, otherwise returns the constructor
 * with the most parameters.
 *
 * @return The found constructor, or null if no suitable constructor exists
 */
internal val KSFunctionDeclaration.parameterTypes: List<KSType> get() = parameters.map { it.type.resolve() }

context(resolver: Resolver)
internal fun KSFunctionDeclaration.injectableConstructors(): Sequence<InjectableConstructor> = when {
    isPublic() -> {
        val injectable = parameters.filter { !it.hasDefault && it.type.resolve().isInjectable() }
        val injectabletypes = injectable.map { it.type.resolve() }.toSet()
        when {
            injectabletypes.size == injectable.size && injectable.none { it.isVararg } -> {
                val defaultParameters = parameters.filter { it.hasDefault }
                injectable.powerSetWithoutEmptySet().flatMap { toInject ->
                    val toInjectSet = toInject.toSet()
                    val valueParameters = parameters - toInjectSet
                    (0..defaultParameters.size).asSequence().map { toDrop ->
                        val defaultParametersToDrop = defaultParameters.drop(toDrop).toSet()
                        InjectableConstructor(
                            this,
                            toInject - defaultParametersToDrop,
                            valueParameters - defaultParametersToDrop,
                        )
                    }
                }
            }
            else -> emptySequence()
        }
    }
    else -> emptySequence()
}
