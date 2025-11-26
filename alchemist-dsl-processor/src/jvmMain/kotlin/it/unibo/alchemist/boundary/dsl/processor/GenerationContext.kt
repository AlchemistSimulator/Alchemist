package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

data class TypeParameterInfo(
    val names: List<String>,
    val bounds: List<String>,
    val classTypeParamNames: List<String> = names,
    val classTypeParamBounds: List<String> = bounds,
)

data class ConstructorInfo(
    val allParameters: List<KSValueParameter>,
    val remainingParams: List<KSValueParameter>,
    val paramsToSkip: Set<Int>,
    val paramNames: List<String>,
    val paramTypes: List<String>,
)

data class InjectionContext(
    val indices: Map<InjectionType, Int>,
    val paramNames: Map<InjectionType, String>,
    val paramTypes: Map<InjectionType, String>,
    val annotationValues: Map<String, Any?>,
    val contextType: ContextType,
    val hasContextParams: Boolean = false,
    val contextParamName: String = "ctx",
)

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
