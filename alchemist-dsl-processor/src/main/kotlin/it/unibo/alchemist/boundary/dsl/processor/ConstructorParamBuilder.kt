package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Builds constructor parameter expressions for DSL builder functions.
 */
object ConstructorParamBuilder {
    /**
     * Builds the list of constructor parameter expressions.
     *
     * @param allParameters All constructor parameters
     * @param remainingParams Parameters that are not injected
     * @param paramsToSkip Set of parameter indices to skip
     * @param paramNames Names of remaining parameters
     * @param injectionIndices Map of injection types to parameter indices
     * @param injectedParamNames Map of injection types to parameter names
     * @param annotationValues Annotation values from BuildDsl
     * @param typeParamNames Type parameter names
     * @param contextType The type of context
     * @param hasContextParams Whether context parameters are present
     * @param contextParamName Name of the context parameter
     * @param injectedParamTypes Map of injection types to parameter types
     * @return List of constructor parameter expressions
     */
    fun buildConstructorParams(
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        contextType: ContextType,
        hasContextParams: Boolean = false,
        contextParamName: String = "ctx",
        injectedParamTypes: Map<InjectionType, String> = emptyMap(),
    ): List<String> = buildConstructorParamsInternal(
        allParameters,
        remainingParams,
        paramsToSkip,
        paramNames,
        injectionIndices,
        injectedParamNames,
        annotationValues,
        typeParamNames,
        injectedParamTypes,
        hasContextParams,
        contextType,
        contextParamName,
    )

    private fun isInjectionIndex(
        injectionType: InjectionType,
        index: Int,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        annotationKey: String,
    ): Boolean = injectionIndices.containsKey(injectionType) &&
        index == injectionIndices[injectionType] &&
        (annotationValues[annotationKey] as? Boolean ?: true)

    private fun buildInjectedParam(
        injectionType: InjectionType,
        param: KSValueParameter,
        hasContextParams: Boolean,
        contextType: ContextType,
        contextParamName: String,
        injectedParamNames: Map<InjectionType, String>,
        injectedParamTypes: Map<InjectionType, String>,
        typeParamNames: List<String>,
    ): String {
        val accessor = buildAccessor(injectionType, hasContextParams, contextType, contextParamName, injectedParamNames)
        val contextParamType = injectedParamTypes[injectionType]
        return if (contextParamType != null && needsCast(param.type, contextParamType, typeParamNames)) {
            val castType = TypeExtractor.extractTypeString(param.type, typeParamNames)
            "$accessor as $castType"
        } else {
            accessor
        }
    }

    private fun buildAccessor(
        injectionType: InjectionType,
        hasContextParams: Boolean,
        contextType: ContextType,
        contextParamName: String,
        injectedParamNames: Map<InjectionType, String>,
    ): String {
        if (!hasContextParams) {
            return injectedParamNames[injectionType] ?: getDefaultParamName(injectionType)
        }

        return ContextAccessor.getAccessor(injectionType, contextType, contextParamName)
    }

    private fun getDefaultParamName(injectionType: InjectionType): String = when (injectionType) {
        InjectionType.ENVIRONMENT -> "env"
        InjectionType.GENERATOR -> "generator"
        InjectionType.INCARNATION -> "incarnation"
        InjectionType.NODE -> "node"
        InjectionType.REACTION -> "reaction"
        InjectionType.TIMEDISTRIBUTION -> "timeDistribution"
    }

    private fun needsCast(
        constructorParamType: KSTypeReference,
        contextParamType: String,
        typeParamNames: List<String>,
    ): Boolean {
        val constructorTypeStr = TypeExtractor.extractTypeString(constructorParamType, typeParamNames)

        if (constructorTypeStr == contextParamType) {
            return false
        }

        val constructorResolved = constructorParamType.resolve()
        val constructorDecl = constructorResolved.declaration
        val constructorQualified = constructorDecl.qualifiedName?.asString().orEmpty()

        return when {
            !contextParamType.startsWith(constructorQualified) -> true
            constructorResolved.arguments.isEmpty() -> false
            else -> checkWildcardCastNeeded(constructorTypeStr, contextParamType, typeParamNames)
        }
    }

    private fun checkWildcardCastNeeded(
        constructorTypeStr: String,
        contextParamType: String,
        typeParamNames: List<String>,
    ): Boolean {
        val constructorHasWildcards = constructorTypeStr.contains("<") &&
            (constructorTypeStr.contains("<*") || constructorTypeStr.contains(", *"))

        if (constructorHasWildcards) {
            val contextTypeArgs = if (contextParamType.contains("<")) {
                contextParamType.substringAfter("<").substringBefore(">")
            } else {
                ""
            }
            return typeParamNames.any { it in contextTypeArgs }
        }

        return constructorTypeStr != contextParamType
    }

    fun buildConstructorParamsForPropertyContext(
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        injectedParamTypes: Map<InjectionType, String> = emptyMap(),
    ): List<String> = buildConstructorParamsInternal(
        allParameters,
        remainingParams,
        paramsToSkip,
        paramNames,
        injectionIndices,
        injectedParamNames,
        annotationValues,
        typeParamNames,
        injectedParamTypes,
        hasContextParams = true,
        contextType = ContextType.PROPERTY,
        contextParamName = "ctx",
    )

    fun convertToPropertyContextAccessors(
        injectionIndices: Map<InjectionType, Int>,
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        injectedParamTypes: Map<InjectionType, String>,
    ): List<String> = buildConstructorParamsInternal(
        allParameters,
        remainingParams,
        paramsToSkip,
        paramNames,
        injectionIndices,
        injectedParamNames,
        annotationValues,
        typeParamNames,
        injectedParamTypes,
        hasContextParams = true,
        contextType = ContextType.PROPERTY,
        contextParamName = "ctx",
    )

    private fun buildConstructorParamsInternal(
        allParameters: List<KSValueParameter>,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        injectedParamTypes: Map<InjectionType, String>,
        hasContextParams: Boolean,
        contextType: ContextType,
        contextParamName: String,
    ): List<String> {
        val constructorParams = mutableListOf<String>()
        var remainingIndex = 0

        allParameters.forEachIndexed { index, param ->
            when {
                isInjectionIndex(
                    InjectionType.ENVIRONMENT,
                    index,
                    injectionIndices,
                    annotationValues,
                    "injectEnvironment",
                ) -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.ENVIRONMENT,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                isInjectionIndex(
                    InjectionType.GENERATOR,
                    index,
                    injectionIndices,
                    annotationValues,
                    "injectGenerator",
                ) -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.GENERATOR,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                isInjectionIndex(
                    InjectionType.INCARNATION,
                    index,
                    injectionIndices,
                    annotationValues,
                    "injectIncarnation",
                ) -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.INCARNATION,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                isInjectionIndex(InjectionType.NODE, index, injectionIndices, annotationValues, "injectNode") -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.NODE,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                isInjectionIndex(
                    InjectionType.REACTION,
                    index,
                    injectionIndices,
                    annotationValues,
                    "injectReaction",
                ) -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.REACTION,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                injectionIndices.containsKey(InjectionType.TIMEDISTRIBUTION) &&
                    index == injectionIndices[InjectionType.TIMEDISTRIBUTION] -> {
                    constructorParams.add(
                        buildInjectedParam(
                            InjectionType.TIMEDISTRIBUTION,
                            param,
                            hasContextParams,
                            contextType,
                            contextParamName,
                            injectedParamNames,
                            injectedParamTypes,
                            typeParamNames,
                        ),
                    )
                }
                !paramsToSkip.contains(index) -> {
                    val paramName = paramNames[remainingIndex]
                    val remainingParam = remainingParams[remainingIndex]
                    val paramValue = if (remainingParam.isVararg) "*$paramName" else paramName
                    constructorParams.add(paramValue)
                    remainingIndex++
                }
                else -> {
                    constructorParams.add("null")
                }
            }
        }
        return constructorParams
    }
}
