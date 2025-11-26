package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter

object ConstructorParamBuilder {
    /**
     * Walks constructor parameters in order and either injects context values
     * or maps supplied arguments.
     */
    fun buildConstructorParams(
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> = buildConstructorParamsInternal(
        constructorInfo,
        injectionContext,
        typeParamNames,
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
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): String {
        val accessor = buildAccessor(injectionType, injectionContext)
        val contextParamType = injectionContext.paramTypes[injectionType]
        return if (contextParamType != null && needsCast(param.type, contextParamType, typeParamNames)) {
            val castType = TypeExtractor.extractTypeString(param.type, typeParamNames)
            "$accessor as $castType"
        } else {
            accessor
        }
    }

    private fun buildAccessor(injectionType: InjectionType, injectionContext: InjectionContext): String {
        if (!injectionContext.hasContextParams) {
            return injectionContext.paramNames[injectionType] ?: getDefaultParamName(injectionType)
        }
        return ContextAccessor.getAccessor(
            injectionType,
            injectionContext.contextType,
            injectionContext.contextParamName,
        )
    }

    private fun getDefaultParamName(injectionType: InjectionType): String = injectionType.name.lowercase()

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
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> {
        val propertyContext = injectionContext.copy(
            hasContextParams = true,
            contextType = ContextType.PROPERTY_CONTEXT,
            contextParamName = "ctx",
        )
        return buildConstructorParamsInternal(
            constructorInfo,
            propertyContext,
            typeParamNames,
        )
    }

    fun convertToPropertyContextAccessors(
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> {
        val propertyCONTEXTContext = injectionContext.copy(
            hasContextParams = true,
            contextType = ContextType.PROPERTY_CONTEXT,
            contextParamName = "ctx",
        )
        return buildConstructorParamsInternal(
            constructorInfo,
            propertyCONTEXTContext,
            typeParamNames,
        )
    }

    private fun buildConstructorParamsInternal(
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> {
        val constructorParams = mutableListOf<String>()
        var remainingIndex = 0
        constructorInfo.allParameters.forEachIndexed { index, param ->
            val paramExpr = buildParamExpression(
                index,
                param,
                remainingIndex,
                constructorInfo,
                injectionContext,
                typeParamNames,
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
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): Pair<String, Boolean> {
        val injectionType = findInjectionType(index, injectionContext, constructorInfo.paramsToSkip)
        if (injectionType != null) {
            return buildInjectedParamExpression(injectionType, param, injectionContext, typeParamNames)
        }
        return buildRegularParamExpression(index, remainingIndex, constructorInfo)
    }

    private fun buildInjectedParamExpression(
        injectionType: InjectionType,
        param: KSValueParameter,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): Pair<String, Boolean> {
        val accessor = buildInjectedParam(injectionType, param, injectionContext, typeParamNames)
        return Pair(accessor, false)
    }

    private fun buildRegularParamExpression(
        index: Int,
        remainingIndex: Int,
        constructorInfo: ConstructorInfo,
    ): Pair<String, Boolean> {
        if (!constructorInfo.paramsToSkip.contains(index)) {
            val paramName = constructorInfo.paramNames[remainingIndex]
            val remainingParam = constructorInfo.remainingParams[remainingIndex]
            val paramValue = if (remainingParam.isVararg) "*$paramName" else paramName
            return Pair(paramValue, true)
        }
        return Pair("null", false)
    }

    private fun findInjectionType(
        index: Int,
        injectionContext: InjectionContext,
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
        return findInjectionTypeFromList(index, injectionContext, paramsToSkip, injectionTypes)
    }

    private fun findInjectionTypeFromList(
        index: Int,
        injectionContext: InjectionContext,
        paramsToSkip: Set<Int>,
        injectionTypes: List<Triple<InjectionType, String, Boolean>>,
    ): InjectionType? {
        for ((type, annotationKey, checkAnnotation) in injectionTypes) {
            if ((
                    checkAnnotation &&
                        isInjectionIndex(
                            type,
                            index,
                            injectionContext.indices,
                            injectionContext.annotationValues,
                            annotationKey,
                        )
                    ) ||
                (
                    !checkAnnotation &&
                        injectionContext.indices.containsKey(type) &&
                        index == injectionContext.indices[type] &&
                        paramsToSkip.contains(index)
                    )
            ) {
                return type
            }
        }
        return null
    }
}
