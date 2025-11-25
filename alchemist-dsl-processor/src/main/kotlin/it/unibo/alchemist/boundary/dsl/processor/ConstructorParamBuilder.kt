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
        annotationValues[annotationKey] as? Boolean ?: true

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
        InjectionType.FILTER -> "filter"
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

    /**
     * Builds constructor parameter expressions for property context.
     *
     * @param allParameters All constructor parameters
     * @param remainingParams Parameters that are not injected
     * @param paramsToSkip Set of parameter indices to skip
     * @param paramNames Names of remaining parameters
     * @param injectionIndices Map of injection types to parameter indices
     * @param injectedParamNames Map of injection types to parameter names
     * @param annotationValues Annotation values from BuildDsl
     * @param typeParamNames Type parameter names
     * @param injectedParamTypes Map of injection types to parameter types
     * @return List of constructor parameter expressions
     */
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

    /**
     * Converts constructor parameters to property context accessors.
     *
     * @param injectionIndices Map of injection types to parameter indices
     * @param allParameters All constructor parameters
     * @param remainingParams Parameters that are not injected
     * @param paramsToSkip Set of parameter indices to skip
     * @param paramNames Names of remaining parameters
     * @param injectedParamNames Map of injection types to parameter names
     * @param annotationValues Annotation values from BuildDsl
     * @param typeParamNames Type parameter names
     * @param injectedParamTypes Map of injection types to parameter types
     * @return List of constructor parameter expressions using property context accessors
     */
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
            val paramExpr = buildParamExpression(
                index,
                param,
                remainingIndex,
                remainingParams,
                paramsToSkip,
                paramNames,
                injectionIndices,
                injectedParamNames,
                injectedParamTypes,
                annotationValues,
                typeParamNames,
                hasContextParams,
                contextType,
                contextParamName,
            )
            constructorParams.add(paramExpr.first)
            if (paramExpr.second) {
                remainingIndex++
            }
        }
        return constructorParams
    }

    private fun buildParamExpression(
        index: Int,
        param: KSValueParameter,
        remainingIndex: Int,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        injectedParamTypes: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        hasContextParams: Boolean,
        contextType: ContextType,
        contextParamName: String,
    ): Pair<String, Boolean> {
        val injectionType = findInjectionType(index, injectionIndices, annotationValues, paramsToSkip)
        if (injectionType != null) {
            return buildInjectedParamExpression(
                injectionType,
                param,
                hasContextParams,
                contextType,
                contextParamName,
                injectedParamNames,
                injectedParamTypes,
                typeParamNames,
            )
        }

        return buildRegularParamExpression(
            index,
            remainingIndex,
            remainingParams,
            paramsToSkip,
            paramNames,
        )
    }

    private fun buildInjectedParamExpression(
        injectionType: InjectionType,
        param: KSValueParameter,
        hasContextParams: Boolean,
        contextType: ContextType,
        contextParamName: String,
        injectedParamNames: Map<InjectionType, String>,
        injectedParamTypes: Map<InjectionType, String>,
        typeParamNames: List<String>,
    ): Pair<String, Boolean> {
        val accessor = buildInjectedParam(
            injectionType,
            param,
            hasContextParams,
            contextType,
            contextParamName,
            injectedParamNames,
            injectedParamTypes,
            typeParamNames,
        )
        return Pair(accessor, false)
    }

    private fun buildRegularParamExpression(
        index: Int,
        remainingIndex: Int,
        remainingParams: List<KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
    ): Pair<String, Boolean> {
        if (!paramsToSkip.contains(index)) {
            val paramName = paramNames[remainingIndex]
            val remainingParam = remainingParams[remainingIndex]
            val paramValue = if (remainingParam.isVararg) "*$paramName" else paramName
            return Pair(paramValue, true)
        }
        return Pair("null", false)
    }

    private fun findInjectionType(
        index: Int,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        paramsToSkip: Set<Int>,
    ): InjectionType? {
        val injectionTypes = listOf(
            Triple(InjectionType.ENVIRONMENT, "injectEnvironment", true),
            Triple(InjectionType.GENERATOR, "injectGenerator", true),
            Triple(InjectionType.INCARNATION, "injectIncarnation", true),
            Triple(InjectionType.NODE, "injectNode", true),
            Triple(InjectionType.REACTION, "injectReaction", true),
            Triple(InjectionType.TIMEDISTRIBUTION, "", false),
            Triple(InjectionType.FILTER, "", false),
        )

        return findInjectionTypeFromList(index, injectionIndices, annotationValues, paramsToSkip, injectionTypes)
    }

    private fun findInjectionTypeFromList(
        index: Int,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        paramsToSkip: Set<Int>,
        injectionTypes: List<Triple<InjectionType, String, Boolean>>,
    ): InjectionType? {
        for ((type, annotationKey, checkAnnotation) in injectionTypes) {
            val found = if (checkAnnotation) {
                isInjectionIndex(type, index, injectionIndices, annotationValues, annotationKey)
            } else {
                injectionIndices.containsKey(type) &&
                    index == injectionIndices[type] &&
                    paramsToSkip.contains(index)
            }
            if (found) {
                return type
            }
        }
        return null
    }
}
