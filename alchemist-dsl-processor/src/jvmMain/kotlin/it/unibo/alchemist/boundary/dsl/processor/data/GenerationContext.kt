/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.processor.data

import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * Aggregates everything needed to emit a DSL helper for a specific class.
 *
 * @property classDecl The declaration being processed.
 * @property className Simple name of the target class.
 * @property functionName Generated helper function name.
 * @property typeParams Type parameter metadata for the helper.
 * @property constructorInfo Constructor metadata derived from the declaration.
 * @property injectionContext Information about which parameters need injection.
 * @property injectedParams List of injected parameter (name,type) pairs.
 * @property defaultValues Default values inferred for remaining params.
 * @property needsMapEnvironment Whether a `MapEnvironment` import is required.
 */
data class GenerationContext(
    val classDecl: KSClassDeclaration,
    val className: String,
    val functionName: String,
    val typeParams: TypeParameterInfo,
    val constructorInfo: ConstructorInfo,
    val injectionContext: InjectionContext,
    val injectedParams: List<Pair<String, String>>,
    val defaultValues: List<String>,
    val needsMapEnvironment: Boolean,
)
