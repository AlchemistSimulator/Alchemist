package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import it.unibo.alchemist.boundary.dsl.processor.data.ConstructorInfo
import it.unibo.alchemist.boundary.dsl.processor.data.InjectionContext
import it.unibo.alchemist.boundary.dsl.processor.data.TypeParameterInfo

/** Emits the signature, call site, and accessor for generated DSL helpers. */
internal object FunctionGenerator {
    /** Builds the helper function signature including context and receiver parts. */
    fun buildFunctionSignature(
        functionName: String,
        className: String,
        typeParams: TypeParameterInfo,
        constructorInfo: ConstructorInfo,
        injectedParams: List<Pair<String, String>>,
        contextType: ContextType,
    ): String {
        val (finalTypeParamNames, finalTypeParamBounds) = TypeParameterHandler.prepareTypeParams(
            typeParams.names,
            typeParams.bounds,
            injectedParams,
            typeParams.classTypeParamBounds,
        )

        val functionTypeParamString = TypeParameterHandler.buildTypeParamString(finalTypeParamBounds)
        val returnType = TypeParameterHandler.buildReturnType(className, typeParams.classTypeParamNames)
        val contextPart = buildContextPart(injectedParams, contextType, finalTypeParamNames, finalTypeParamBounds)
        val functionParams = buildFunctionParams(
            constructorInfo.remainingParams,
            constructorInfo.paramNames,
            constructorInfo.paramTypes,
        )
        val receiverPart = buildReceiverPart(injectedParams, contextType)
        return "${contextPart}fun$functionTypeParamString $receiverPart$functionName$functionParams: $returnType ="
    }

    // Build the context receiver only when injections exist so the generated function can require ctx.
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
        val pWithVariance = if (pVariance.isNotEmpty()) "$pVariance $pParam" else pParam
        val contextTypeName = when (contextType) {
            ContextType.SIMULATION_CONTEXT ->
                "${ProcessorConfig.ContextTypes.SIMULATION_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.EXPORTER_CONTEXT ->
                "${ProcessorConfig.ContextTypes.EXPORTER_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.GLOBAL_PROGRAMS_CONTEXT ->
                "${ProcessorConfig.ContextTypes.GLOBAL_PROGRAMS_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.OUTPUT_MONITORS_CONTEXT ->
                "${ProcessorConfig.ContextTypes.OUTPUT_MONITORS_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.TERMINATORS_CONTEXT ->
                "${ProcessorConfig.ContextTypes.TERMINATORS_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.DEPLOYMENTS_CONTEXT ->
                "${ProcessorConfig.ContextTypes.DEPLOYMENTS_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.DEPLOYMENT_CONTEXT ->
                "${ProcessorConfig.ContextTypes.DEPLOYMENT_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.PROGRAM_CONTEXT -> "${ProcessorConfig.ContextTypes.PROGRAM_CONTEXT}<$tParam, $pWithVariance>"
            ContextType.PROPERTY_CONTEXT -> "${ProcessorConfig.ContextTypes.PROPERTY_CONTEXT}<$tParam, $pWithVariance>"
        }
        return "context(ctx: $contextTypeName) "
    }

    /** Reads the variance annotation (in/out) declared for a type parameter bound. */
    fun extractVarianceFromBound(paramName: String, typeParamBounds: List<String>): String {
        val paramIndex = typeParamBounds.indexOfFirst { it.startsWith("$paramName:") }
        if (paramIndex < 0) {
            return ""
        }
        val bound = typeParamBounds[paramIndex]
        val boundPart = bound.substringAfter(":", "").trim()
        return extractVarianceFromBoundPart(paramName, boundPart)
    }

    private fun extractVarianceFromBoundPart(paramName: String, boundPart: String): String {
        val escapedParamName = Regex.escape(paramName)
        val outPattern = Regex("""<out\s+$escapedParamName>""")
        val inPattern = Regex("""<in\s+$escapedParamName>""")
        return when {
            outPattern.containsMatchIn(boundPart) || boundPart.startsWith("out ") -> "out"
            inPattern.containsMatchIn(boundPart) || boundPart.startsWith("in ") -> "in"
            else -> ""
        }
    }

    /** Returns the parameter list string for the remaining constructor arguments. */
    fun buildFunctionParams(
        remainingParams: List<KSValueParameter>,
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

    private const val EMPTY_RECEIVER = ""
    private fun buildReceiverPart(
        @Suppress("UNUSED_PARAMETER") injectedParams: List<Pair<String, String>>,
        @Suppress("UNUSED_PARAMETER") contextType: ContextType,
    ): String = EMPTY_RECEIVER

    /** Delegates to [ConstructorParamBuilder] to build argument expressions. */
    fun buildConstructorParams(
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> = ConstructorParamBuilder.buildConstructorParams(
        constructorInfo,
        injectionContext,
        typeParamNames,
    )

    /** Formats the constructor invocation using the resolved parameters. */
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

    private fun extractDefaultValue(param: KSValueParameter): String {
        if (!param.hasDefault) {
            return ""
        }
        val defaultValueExpr = DefaultValueAnalyzer.tryExtractDefaultFromSource(param)
        return defaultValueExpr?.let { " = $it" }.orEmpty()
    }

    /** Computes which type parameters the referenced type requires beyond the ones already known. */
    fun collectNeededTypeParams(typeRef: KSTypeReference, existingTypeParamNames: List<String>): Set<String> =
        TypeParameterHandler.collectNeededTypeParams(typeRef, existingTypeParamNames)

    /** Builds the context parameter type for the injected argument expression. */
    fun buildContextParamType(
        typeRef: KSTypeReference,
        typeParamNames: MutableList<String>,
        typeParamBounds: MutableList<String>,
    ): String = TypeArgumentProcessor.buildContextParamType(typeRef, typeParamNames, typeParamBounds)

    /** Builds property-context-specific constructor arguments. */
    fun buildConstructorParamsForPropertyContext(
        constructorInfo: ConstructorInfo,
        injectionContext: InjectionContext,
        typeParamNames: List<String>,
    ): List<String> = ConstructorParamBuilder.buildConstructorParamsForPropertyContext(
        constructorInfo,
        injectionContext,
        typeParamNames,
    )
}
