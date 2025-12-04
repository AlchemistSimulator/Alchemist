package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Captures the type parameter names and bounds needed inside the generated helper.
 *
 * @property names Generated type parameter identifiers.
 * @property bounds Bounds for each generated type parameter.
 * @property classTypeParamNames Original class-level type parameter names.
 * @property classTypeParamBounds Original class-level type parameter bounds.
 */
data class TypeParameterInfo(
    val names: List<String>,
    val bounds: List<String>,
    val classTypeParamNames: List<String> = names,
    val classTypeParamBounds: List<String> = bounds,
)

/**
 * Describes the constructor parameters before and after injection filtering.
 *
 * @property allParameters All parameters declared on the constructor.
 * @property remainingParams Parameters the caller still needs to provide.
 * @property paramsToSkip Indexes of injected parameters inside the constructor.
 * @property paramNames Names assigned to the remaining parameters.
 * @property paramTypes Types of the remaining parameters.
 */
data class ConstructorInfo(
    val allParameters: List<KSValueParameter>,
    val remainingParams: List<KSValueParameter>,
    val paramsToSkip: Set<Int>,
    val paramNames: List<String>,
    val paramTypes: List<String>,
)

/**
 * Represents the injected values and annotation-driven flags available during generation.
 *
 * @property indices Mapping of injection types to constructor indexes.
 * @property paramNames Local names allocated to injected context parameters.
 * @property paramTypes Types assigned to injected context parameters.
 * @property annotationValues Extracted values from the `@BuildDsl` annotation.
 * @property contextType Chosen context enum describing the current execution environment.
 * @property hasContextParams Whether the helper defines context receivers.
 * @property contextParamName Name of the context parameter used by accessors.
 */
data class InjectionContext(
    val indices: Map<InjectionType, Int>,
    val paramNames: Map<InjectionType, String>,
    val paramTypes: Map<InjectionType, String>,
    val annotationValues: Map<String, Any?>,
    val contextType: ContextType,
    val hasContextParams: Boolean = false,
    val contextParamName: String = "ctx",
)

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
