package it.unibo.alchemist.boundary.dsl.processor

/**
 * Generates function signatures and constructor calls for DSL builder functions.
 */
object FunctionGenerator {
    /**
     * Builds the function signature for a DSL builder function.
     *
     * @param functionName The name of the function to generate
     * @param typeParamBounds Type parameter bounds
     * @param typeParamNames Type parameter names
     * @param className The name of the class being constructed
     * @param remainingParams Parameters that are not injected
     * @param paramNames Names of the remaining parameters
     * @param paramTypes Types of the remaining parameters
     * @param injectedParams List of injected parameter names and types
     * @param contextType The type of context (deployment or program)
     * @param classTypeParamNames Type parameter names from the class declaration
     * @return The complete function signature string
     */
    fun buildFunctionSignature(
        functionName: String,
        typeParamBounds: List<String>,
        typeParamNames: List<String>,
        className: String,
        remainingParams: List<com.google.devtools.ksp.symbol.KSValueParameter>,
        paramNames: List<String>,
        paramTypes: List<String>,
        injectedParams: List<Pair<String, String>>,
        contextType: ContextType,
        classTypeParamNames: List<String> = typeParamNames,
        classTypeParamBounds: List<String> = typeParamBounds,
    ): String {
        val (finalTypeParamNames, finalTypeParamBounds) = TypeParameterHandler.prepareTypeParams(
            typeParamNames,
            typeParamBounds,
            injectedParams,
            classTypeParamBounds,
        )

        val functionTypeParamString = TypeParameterHandler.buildTypeParamString(finalTypeParamBounds)
        val returnType = TypeParameterHandler.buildReturnType(className, classTypeParamNames)
        val contextPart = buildContextPart(injectedParams, contextType, finalTypeParamNames, finalTypeParamBounds)
        val functionParams = buildFunctionParams(remainingParams, paramNames, paramTypes)
        val receiverPart = buildReceiverPart(injectedParams, contextType)

        return "${contextPart}fun$functionTypeParamString $receiverPart$functionName$functionParams: $returnType ="
    }

    private fun buildContextPart(
        injectedParams: List<Pair<String, String>>,
        contextType: ContextType,
        typeParamNames: List<String>,
        typeParamBounds: List<String>,
    ): String {
        if (injectedParams.isEmpty()) {
            return ""
        }

        val (tParam, pParam) = TypeParameterHandler.findTAndPParams(typeParamNames, typeParamBounds)
        val pVariance = extractVarianceFromBound(pParam, typeParamBounds)
        val tWithVariance = tParam
        val pWithVariance = if (pVariance.isNotEmpty()) "$pVariance $pParam" else pParam
        val contextTypeName = when (contextType) {
            ContextType.SIMULATION -> "SimulationContext<$tWithVariance, $pWithVariance>"
            ContextType.DEPLOYMENT -> "DeploymentsContext<$tWithVariance, $pWithVariance>"
            ContextType.PROGRAM -> "ProgramsContext<$tWithVariance, $pWithVariance>.ProgramContext"
            ContextType.PROPERTY -> "PropertiesContext<$tWithVariance, $pWithVariance>.PropertyContext"
        }
        return "context(ctx: $contextTypeName) "
    }

    fun extractVarianceFromBound(paramName: String, typeParamBounds: List<String>): String {
        val paramIndex = typeParamBounds.indexOfFirst { it.startsWith("$paramName:") }
        if (paramIndex < 0) {
            return ""
        }
        val bound = typeParamBounds[paramIndex]
        val boundPart = bound.substringAfter(":", "").trim()

        val escapedParamName = Regex.escape(paramName)
        val outPattern = Regex("""<out\s+$escapedParamName>""")
        val inPattern = Regex("""<in\s+$escapedParamName>""")

        if (outPattern.containsMatchIn(boundPart)) {
            return "out"
        }
        if (inPattern.containsMatchIn(boundPart)) {
            return "in"
        }

        if (boundPart.startsWith("out ")) {
            return "out"
        }
        if (boundPart.startsWith("in ")) {
            return "in"
        }
        return ""
    }

    fun buildFunctionParams(
        remainingParams: List<com.google.devtools.ksp.symbol.KSValueParameter>,
        paramNames: List<String>,
        paramTypes: List<String>,
    ): String {
        val regularParams = remainingParams.mapIndexed { idx, param ->
            val name = paramNames[idx]
            val type = paramTypes[idx]
            val varargKeyword = if (param.isVararg) "vararg " else ""
            val defaultValue = extractDefaultValue(param)
            "$varargKeyword$name: $type$defaultValue"
        }

        val regularParamsPart = regularParams.joinToString(", ")
        return if (regularParamsPart.isEmpty()) {
            "()"
        } else {
            "($regularParamsPart)"
        }
    }

    private fun buildReceiverPart(injectedParams: List<Pair<String, String>>, contextType: ContextType): String = ""

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
    // CPD-OFF: Function signature duplication is necessary for API delegation
    fun buildConstructorParams(
        allParameters: List<com.google.devtools.ksp.symbol.KSValueParameter>,
        remainingParams: List<com.google.devtools.ksp.symbol.KSValueParameter>,
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
    ): List<String> = ConstructorParamBuilder.buildConstructorParams(
        allParameters,
        remainingParams,
        paramsToSkip,
        paramNames,
        injectionIndices,
        injectedParamNames,
        annotationValues,
        typeParamNames,
        contextType,
        hasContextParams,
        contextParamName,
        injectedParamTypes,
    )
    // CPD-ON

    /**
     * Builds the constructor call expression.
     *
     * @param className The name of the class
     * @param typeParamNames Type parameter names
     * @param constructorParams List of constructor parameter expressions
     * @param classTypeParamNames Type parameter names from the class declaration
     * @return The constructor call string
     */
    fun buildConstructorCall(
        className: String,
        typeParamNames: List<String>,
        constructorParams: List<String>,
        classTypeParamNames: List<String> = typeParamNames,
    ): String {
        val constructorTypeArgs = if (classTypeParamNames.isNotEmpty()) {
            "<${classTypeParamNames.joinToString(", ")}>"
        } else {
            ""
        }
        return "$className$constructorTypeArgs(${constructorParams.joinToString(", ")})"
    }

    private fun extractDefaultValue(param: com.google.devtools.ksp.symbol.KSValueParameter): String {
        if (!param.hasDefault) {
            return ""
        }

        val defaultValueExpr = DefaultValueAnalyzer.tryExtractDefaultFromSource(param)
        return defaultValueExpr?.let { " = $it" }.orEmpty()
    }

    /**
     * Collects type parameters needed for a type reference.
     *
     * @param typeRef The type reference to analyze
     * @param existingTypeParamNames List of existing type parameter names
     * @return Set of needed type parameter names
     */
    fun collectNeededTypeParams(
        typeRef: com.google.devtools.ksp.symbol.KSTypeReference,
        existingTypeParamNames: List<String>,
    ): Set<String> = TypeParameterHandler.collectNeededTypeParams(typeRef, existingTypeParamNames)

    /**
     * Builds the type string for a context parameter, handling type arguments and bounds.
     *
     * @param typeRef The type reference to build from
     * @param typeParamNames Mutable list of type parameter names (may be modified)
     * @param typeParamBounds Mutable list of type parameter bounds (may be modified)
     * @return The type string for the context parameter
     */
    fun buildContextParamType(
        typeRef: com.google.devtools.ksp.symbol.KSTypeReference,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ): String = TypeArgumentProcessor.buildContextParamType(typeRef, typeParamNames, typeParamBounds)

    fun buildConstructorParamsForPropertyContext(
        allParameters: List<com.google.devtools.ksp.symbol.KSValueParameter>,
        remainingParams: List<com.google.devtools.ksp.symbol.KSValueParameter>,
        paramsToSkip: Set<Int>,
        paramNames: List<String>,
        injectionIndices: Map<InjectionType, Int>,
        injectedParamNames: Map<InjectionType, String>,
        annotationValues: Map<String, Any?>,
        typeParamNames: List<String>,
        injectedParamTypes: Map<InjectionType, String> = emptyMap(),
    ): List<String> = ConstructorParamBuilder.buildConstructorParamsForPropertyContext(
        allParameters,
        remainingParams,
        paramsToSkip,
        paramNames,
        injectionIndices,
        injectedParamNames,
        annotationValues,
        typeParamNames,
        injectedParamTypes,
    )
}
