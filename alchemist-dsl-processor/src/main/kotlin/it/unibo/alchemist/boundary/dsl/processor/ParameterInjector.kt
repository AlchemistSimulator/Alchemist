package it.unibo.alchemist.boundary.dsl.processor

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
                isTimeDistributionType(resolved, simpleName, qualifiedName) -> indices[InjectionType.TIMEDISTRIBUTION] =
                    index
            }
        }

        return indices
    }

    private fun isEnvironmentType(type: com.google.devtools.ksp.symbol.KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("Environment")) {
            return false
        }
        return TypeHierarchyChecker.matchesTypeOrPackage(
            type,
            ProcessorConfig.ENVIRONMENT_TYPE,
            ProcessorConfig.ENVIRONMENT_PACKAGE_PATTERNS,
        )
    }

    private fun isGeneratorType(type: com.google.devtools.ksp.symbol.KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("RandomGenerator")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.RANDOM_GENERATOR_TYPE) ||
            qualifiedName == ProcessorConfig.RANDOM_GENERATOR_TYPE
    }

    private fun isIncarnationType(type: com.google.devtools.ksp.symbol.KSType, qualifiedName: String): Boolean {
        if (!qualifiedName.contains("Incarnation")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.INCARNATION_TYPE) ||
            qualifiedName == ProcessorConfig.INCARNATION_TYPE
    }

    private fun isNodeType(
        type: com.google.devtools.ksp.symbol.KSType,
        simpleName: String,
        qualifiedName: String,
    ): Boolean {
        if (simpleName != "Node" && !qualifiedName.endsWith(".Node")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.NODE_TYPE) ||
            qualifiedName == ProcessorConfig.NODE_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.NODE_TYPE}.")
    }

    private fun isReactionType(
        type: com.google.devtools.ksp.symbol.KSType,
        simpleName: String,
        qualifiedName: String,
    ): Boolean {
        if (simpleName != "Reaction" && !qualifiedName.endsWith(".Reaction")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.REACTION_TYPE) ||
            qualifiedName == ProcessorConfig.REACTION_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.REACTION_TYPE}.")
    }

    private fun isTimeDistributionType(
        type: com.google.devtools.ksp.symbol.KSType,
        simpleName: String,
        qualifiedName: String,
    ): Boolean {
        if (simpleName != "TimeDistribution" && !qualifiedName.endsWith(".TimeDistribution")) {
            return false
        }
        return TypeHierarchyChecker.isAssignableTo(type, ProcessorConfig.TIME_DISTRIBUTION_TYPE) ||
            qualifiedName == ProcessorConfig.TIME_DISTRIBUTION_TYPE ||
            qualifiedName.startsWith("${ProcessorConfig.TIME_DISTRIBUTION_TYPE}.")
    }

    /**
     * Determines the context type based on injection indices and annotation values.
     *
     * @param injectionIndices Map of injection types to parameter indices
     * @param annotationValues Annotation values from the BuildDsl annotation
     * @return The determined context type
     */
    fun determineContextType(
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
    ): ContextType {
        val hasNode = injectionIndices.containsKey(InjectionType.NODE) &&
            (annotationValues["injectNode"] as? Boolean ?: true)
        val hasReaction = injectionIndices.containsKey(InjectionType.REACTION) &&
            (annotationValues["injectReaction"] as? Boolean ?: true)
        val hasTimeDistribution = injectionIndices.containsKey(InjectionType.TIMEDISTRIBUTION)

        if (hasNode || hasReaction || hasTimeDistribution) {
            return ContextType.PROGRAM
        }

        val hasEnvironment = injectionIndices.containsKey(InjectionType.ENVIRONMENT) &&
            (annotationValues["injectEnvironment"] as? Boolean ?: true)
        val hasGenerator = injectionIndices.containsKey(InjectionType.GENERATOR) &&
            (annotationValues["injectGenerator"] as? Boolean ?: true)
        val hasIncarnation = injectionIndices.containsKey(InjectionType.INCARNATION) &&
            (annotationValues["injectIncarnation"] as? Boolean ?: true)

        val injectedCount = listOf(hasEnvironment, hasGenerator, hasIncarnation).count { it }

        return if (injectedCount == 1 && (hasIncarnation || hasEnvironment)) {
            ContextType.SIMULATION
        } else {
            ContextType.DEPLOYMENT
        }
    }

    /**
     * Gets the set of parameter indices that should be skipped (injected from context).
     *
     * @param injectionIndices Map of injection types to parameter indices
     * @param annotationValues Annotation values from the BuildDsl annotation
     * @return Set of parameter indices to skip
     */
    fun getInjectionParams(injectionIndices: Map<InjectionType, Int>, annotationValues: Map<String, Any?>): Set<Int> {
        val paramsToSkip = mutableSetOf<Int>()

        addInjectionParamIfEnabled(
            InjectionType.ENVIRONMENT,
            "injectEnvironment",
            injectionIndices,
            annotationValues,
            paramsToSkip,
        )

        addInjectionParamIfEnabled(
            InjectionType.GENERATOR,
            "injectGenerator",
            injectionIndices,
            annotationValues,
            paramsToSkip,
        )

        addInjectionParamIfEnabled(
            InjectionType.INCARNATION,
            "injectIncarnation",
            injectionIndices,
            annotationValues,
            paramsToSkip,
        )

        addInjectionParamIfEnabled(
            InjectionType.NODE,
            "injectNode",
            injectionIndices,
            annotationValues,
            paramsToSkip,
        )

        addInjectionParamIfEnabled(
            InjectionType.REACTION,
            "injectReaction",
            injectionIndices,
            annotationValues,
            paramsToSkip,
        )

        injectionIndices[InjectionType.TIMEDISTRIBUTION]?.let { paramsToSkip.add(it) }

        return paramsToSkip
    }

    private fun addInjectionParamIfEnabled(
        injectionType: InjectionType,
        annotationKey: String,
        injectionIndices: Map<InjectionType, Int>,
        annotationValues: Map<String, Any?>,
        paramsToSkip: MutableSet<Int>,
    ) {
        if ((annotationValues[annotationKey] as? Boolean ?: true) &&
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
    SIMULATION,

    /** Deployment context (environment and generator). */
    DEPLOYMENT,

    /** Program context (includes node, reaction, and time distribution). */
    PROGRAM,

    /** Property context (includes node, same depth as program context). */
    PROPERTY,
}
