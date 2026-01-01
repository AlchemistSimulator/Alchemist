/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.extensions

import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import it.unibo.alchemist.boundary.dsl.processor.DslBuilderProcessor
import it.unibo.alchemist.boundary.dsl.processor.datatypes.TypePosition

context(resolver: Resolver)
internal fun KSType.isInjectable() = DslBuilderProcessor.injectableTypes().any { it.isAssignableFrom(this) }

context(valid: Set<KSTypeParameter>)
internal fun KSType.render(
    position: TypePosition,
    substitutions: Map<KSTypeParameter, KSTypeArgument> = emptyMap(),
): String = buildString {
    when (val typedDeclaration = declaration) {
        is KSTypeAlias -> {
            // map alias params -> use-site args, then render RHS under that substitution
            val aliasMap = declaration.typeParameters.zip(arguments).toMap()
            val expanded = typedDeclaration.type.resolve()
            append(expanded.render(position, substitutions + aliasMap))
        }
        is KSTypeParameter -> {
            val replacement = substitutions[typedDeclaration]
            when {
                replacement != null -> append(replacement.renderAsTypeArg(substitutions))
                typedDeclaration in valid -> append(declaration.simpleName.asString())
                position == TypePosition.TYPE_ARG -> append("*")
                else -> {
                    // fallback for non-arg position (avoid emitting bare '*', which is illegal)
                    val fallback = typedDeclaration.bounds.firstOrNull()?.resolve()
                    append(fallback?.render(TypePosition.TYPE, substitutions) ?: "Any?")
                }
            }
        }
        is KSClassDeclaration -> {
            append(typedDeclaration.typeName)
            // Protect against mismatched counts (inner classes / typealias quirks):
            val declParamCount = typedDeclaration.typeParameters.size
            val args = (this@render.innerArguments.ifEmpty { this@render.arguments })
                .take(declParamCount)
            if (args.isNotEmpty()) {
                append(args.joinToString(prefix = "<", postfix = ">") { it.renderAsTypeArg(substitutions) })
            }
        }
        else -> error("Unsupported type: $typedDeclaration")
    }

    if (isMarkedNullable) append("?")
}
