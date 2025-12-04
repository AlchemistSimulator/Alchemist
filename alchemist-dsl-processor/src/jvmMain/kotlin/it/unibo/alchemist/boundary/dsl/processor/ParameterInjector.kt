package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Types of parameters that can be injected from context.
 */
enum class InjectionType {
    /** Environment parameter injection. */
    ENVIRONMENT,

    /** Random generator parameter injection. */
    GENERATOR,

    /** Incarnation parameter injection. */
    INCARNATION,

    /** Node parameter injection. */
    NODE,

    /** Reaction parameter injection. */
    REACTION,

    /** Time distribution parameter injection. */
    TIMEDISTRIBUTION,

    /** Position-based filter parameter injection. */
    FILTER,
}

/**
 * Information about a parameter injection.
 *
 * @property type The type of injection
 * @property index The parameter index
 * @property inject Whether to inject this parameter
 */
data class InjectionInfo(
    /** The type of injection. */
    val type: InjectionType,
    /** The parameter index. */
    val index: Int,
    /** Whether to inject this parameter. */
    val inject: Boolean,
)

/**
 * Finds and manages parameter injection indices for context-aware DSL generation.
 */
object ParameterInjector {
    /**
     * Finds indices of parameters that can be injected from context.
     *
     * @param parameters The list of constructor parameters to analyze
     * @return A map from injection type to parameter index
     */
    fun findInjectionIndices(parameters: List<KSValueParameter>): Map<InjectionType, Int> {
        val indices = mutableMapOf<InjectionType, Int>()
        parameters.forEachIndexed { index, param ->
            val resolved = param.type.resolve()
            val declaration = resolved.declaration
            val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
            val simpleName = declaration.simpleName.asString()
            when {
                isEnvironmentType(resolved, qualifiedName) -> indices[InjectionType.ENVIRONMENT] = index
                isGeneratorType(resolved, qualifiedName) -> indices[InjectionType.GENERATOR] = index
                isIncarnationType(resolved, qualifiedName) -> indices[InjectionType.INCARNATION] = index
                isNodeType(resolved, simpleName, qualifiedName) -> indices[InjectionType.NODE] = index
                isReactionType(resolved, simpleName, qualifiedName) -> indices[InjectionType.REACTION] = index
                isTimeDistributionType(resolved, simpleName, qualifiedName) ->
                    indices[InjectionType.TIMEDISTRIBUTION] = index
                isFilterType(resolved, simpleName, qualifiedName) -> indices[InjectionType.FILTER] = index
            }
        }
        return indices
    }

    private fun isEnvironmentType(type: KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("Environment")) {
            return false
        }
        return TypeHierarchyChecker.matchesTypeOrPackage(
            type,
            ProcessorConfig.ENVIRONMENT_TYPE,
            ProcessorConfig.ENVIRONMENT_PACKAGE_PATTERNS,
        )
    }

    private fun isGeneratorType(type: KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("RandomGenerator")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.RANDOM_GENERATOR_TYPE) ||
            qualifiedName == ProcessorConfig.RANDOM_GENERATOR_TYPE
    }

    private fun isIncarnationType(type: KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("Incarnation")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.INCARNATION_TYPE) ||
            qualifiedName == ProcessorConfig.INCARNATION_TYPE
    }

    private fun isNodeType(type: KSType, simpleName: String, qualifiedName: String): Boolean {
        if (simpleName != "Node" && !qualifiedName.endsWith(".Node")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.NODE_TYPE) ||
            qualifiedName == ProcessorConfig.NODE_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.NODE_TYPE}.")
    }

    private fun isReactionType(type: KSType, simpleName: String, qualifiedName: String): Boolean {
        if (simpleName != "Reaction" && !qualifiedName.endsWith(".Reaction")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.REACTION_TYPE) ||
            qualifiedName == ProcessorConfig.REACTION_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.REACTION_TYPE}.")
    }

    private fun isTimeDistributionType(type: KSType, simpleName: String, qualifiedName: String): Boolean {
        if (simpleName != "TimeDistribution" && !qualifiedName.endsWith(".TimeDistribution")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.TIME_DISTRIBUTION_TYPE) ||
            qualifiedName == ProcessorConfig.TIME_DISTRIBUTION_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.TIME_DISTRIBUTION_TYPE}.")
    }

    private fun isFilterType(type: KSType, simpleName: String, qualifiedName: String): Boolean {
        val effectiveType = type.makeNotNullable()
        val effectiveDeclaration = effectiveType.declaration
        val effectiveQualifiedName = effectiveDeclaration.qualifiedName?.asString().orEmpty()
        val effectiveSimpleName = effectiveDeclaration.simpleName.asString()
        if (effectiveSimpleName != "PositionBasedFilter" && !effectiveQualifiedName.endsWith(".PositionBasedFilter")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(effectiveType, ProcessorConfig.POSITION_BASED_FILTER_TYPE) ||
            effectiveQualifiedName == ProcessorConfig.POSITION_BASED_FILTER_TYPE ||
            effectiveQualifiedName.startsWith("${ProcessorConfig.POSITION_BASED_FILTER_TYPE}.")
    }

    /**
     * Parses a scope string to a ContextType enum value.
     * Supports both enum names and class names (e.g., "DEPLOYMENT" or "DEPLOYMENTS_CONTEXT").
     *
     * @param scope The scope string
     * @return The corresponding ContextType, or null if the scope is invalid or empty
     */
    fun parseScope(scope: String?): ContextType? {
        if (scope.isNullOrBlank()) {
            return null
        }
        // accept both versions or the scope
        return when (val upperScope = scope.uppercase()) {
            "SIMULATION", "SIMULATION_CONTEXT" -> ContextType.SIMULATION_CONTEXT
            "EXPORTER", "EXPORTER_CONTEXT" -> ContextType.EXPORTER_CONTEXT
            "GLOBAL_PROGRAMS", "GLOBAL_PROGRAMS_CONTEXT" -> ContextType.GLOBAL_PROGRAMS_CONTEXT
            "OUTPUT_MONITORS", "OUTPUT_MONITORS_CONTEXT" -> ContextType.OUTPUT_MONITORS_CONTEXT
            "TERMINATORS", "TERMINATORS_CONTEXT" -> ContextType.TERMINATORS_CONTEXT
            "DEPLOYMENT", "DEPLOYMENTS_CONTEXT" -> ContextType.DEPLOYMENTS_CONTEXT
            "DEPLOYMENT_CONTEXT" -> ContextType.DEPLOYMENT_CONTEXT
            "PROGRAM", "PROGRAM_CONTEXT" -> ContextType.PROGRAM_CONTEXT
            "PROPERTY", "PROPERTY_CONTEXT" -> ContextType.PROPERTY_CONTEXT
            else -> {
                @Suppress("SwallowedException")
                try {
                    ContextType.valueOf(upperScope)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
        }
    }

    /**
     * Determines the context type based on injection indices and annotation values.
     * If a manual scope is provided in the annotation, it takes precedence over automatic detection.
     *
     * @param injectionIndices Map of injection types to parameter indices
     * @param annotationValues Annotation values from the AlchemistKotlinDSL annotation
     * @return The determined context type
     */
    // Determine which context is active by checking manual overrides first, then injection mix.
    fun determineContextType(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
    ): ContextType {
        val manualScope = annotationValues["scope"] as? String
        if (manualScope != null && manualScope.isNotBlank()) {
            val parsedScope = parseScope(manualScope)
            if (parsedScope != null) {
                return parsedScope
            }
        }
        return determineContextTypeFromInjections(injectionIndices, annotationValues)
    }

    private fun determineContextTypeFromInjections(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
    ): ContextType = when {
        injectionIndices.containsKey(InjectionType.FILTER) -> ContextType.DEPLOYMENT_CONTEXT
        hasProgramContextInjections(injectionIndices, annotationValues) -> ContextType.PROGRAM_CONTEXT
        else -> determineDeploymentOrSimulationContext(injectionIndices, annotationValues)
    }

    private fun hasProgramContextInjections(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
    ): Boolean {
        val hasNode = injectionIndices.containsKey(InjectionType.NODE) &&
            annotationValues["injectNode"] as? Boolean ?: true
        val hasReaction = injectionIndices.containsKey(InjectionType.REACTION) &&
            annotationValues["injectReaction"] as? Boolean ?: true
        val hasTimeDistribution = injectionIndices.containsKey(InjectionType.TIMEDISTRIBUTION)
        return hasNode || hasReaction || hasTimeDistribution
    }

    private fun determineDeploymentOrSimulationContext(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
    ): ContextType {
        val hasEnvironment = injectionIndices.containsKey(InjectionType.ENVIRONMENT) &&
            annotationValues["injectEnvironment"] as? Boolean ?: true
        val hasGenerator = injectionIndices.containsKey(InjectionType.GENERATOR) &&
            annotationValues["injectGenerator"] as? Boolean ?: true
        val hasIncarnation = injectionIndices.containsKey(InjectionType.INCARNATION) &&
            annotationValues["injectIncarnation"] as? Boolean ?: true
        val injectedCount = listOf(hasEnvironment, hasGenerator, hasIncarnation).count { it }
        return if (injectedCount == 1 && (hasIncarnation || hasEnvironment)) {
            ContextType.SIMULATION_CONTEXT
        } else {
            ContextType.DEPLOYMENTS_CONTEXT
        }
    }

    /**
     * Gets the set of parameter indices that should be skipped (injected from context).
     *
     * @param injectionIndices Map of injection types to parameter indices
     * @param annotationValues Annotation values from the AlchemistKotlinDSL annotation
     * @param contextType The context type to determine which parameters can be injected
     * @return Set of parameter indices to skip
     */
    fun getInjectionParams(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        contextType: ContextType,
    ): Set<Int> {
        val paramsToSkip = mutableSetOf<Int>()
        if (isInjectionTypeAvailable(InjectionType.ENVIRONMENT, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.ENVIRONMENT,
                "injectEnvironment",
                injectionIndices,
                annotationValues,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.GENERATOR, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.GENERATOR,
                "injectGenerator",
                injectionIndices,
                annotationValues,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.INCARNATION, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.INCARNATION,
                "injectIncarnation",
                injectionIndices,
                annotationValues,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.NODE, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.NODE,
                "injectNode",
                injectionIndices,
                annotationValues,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.REACTION, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.REACTION,
                "injectReaction",
                injectionIndices,
                annotationValues,
                paramsToSkip,
            )
        }
        if (isInjectionTypeAvailable(InjectionType.TIMEDISTRIBUTION, contextType)) {
            injectionIndices[InjectionType.TIMEDISTRIBUTION]?.let { paramsToSkip.add(it) }
        }
        if (isInjectionTypeAvailable(InjectionType.FILTER, contextType)) {
            injectionIndices[InjectionType.FILTER]?.let { paramsToSkip.add(it) }
        }
        return paramsToSkip
    }

    @Suppress("SwallowedException")
    private fun isInjectionTypeAvailable(injectionType: InjectionType, contextType: ContextType): Boolean = try {
        ContextAccessor.getAccessor(injectionType, contextType)
        true
    } catch (e: IllegalArgumentException) {
        false
    }

    private fun addInjectionParamIfEnabled(
        injectionType: InjectionType,
        annotationKey: String,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        paramsToSkip: MutableSet<Int>,
    ) {
        if (annotationValues[annotationKey] as? Boolean ?: true &&
            injectionIndices.containsKey(injectionType)
        ) {
            injectionIndices[injectionType]?.let { paramsToSkip.add(it) }
        }
    }
}

/**
 * Type of context available for parameter injection.
 */
enum class ContextType {
    /** Simulation context (only incarnation or only environment). */
    SIMULATION_CONTEXT,

    /** Exporter context (one level below SimulationContext, manually settable). */
    EXPORTER_CONTEXT,

    /** Global programs context (one level below SimulationContext, manually settable). */
    GLOBAL_PROGRAMS_CONTEXT,

    /** Output monitors context (one level below SimulationContext, manually settable). */
    OUTPUT_MONITORS_CONTEXT,

    /** Terminators context (one level below SimulationContext, manually settable). */
    TERMINATORS_CONTEXT,

    /** Deployments context (generator). */
    DEPLOYMENTS_CONTEXT,

    /** Deployment context (singular, one level below DeploymentsContext, includes filter). */
    DEPLOYMENT_CONTEXT,

    /** Program context (includes node, reaction, and time distribution). */
    PROGRAM_CONTEXT,

    /** Property context (includes node, same depth as program context). */
    PROPERTY_CONTEXT,
}
