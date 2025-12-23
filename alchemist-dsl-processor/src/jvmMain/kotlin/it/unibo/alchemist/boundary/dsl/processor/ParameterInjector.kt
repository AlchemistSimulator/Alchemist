package it.unibo.alchemist.boundary.dsl.processor

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import it.unibo.alchemist.boundary.dsl.processor.data.InjectionType
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TimeDistribution
import org.apache.commons.math3.random.RandomGenerator

/**
 * Information about a parameter injection.
 *
 * @property type The type of injection
 * @property index The parameter index
 * @property inject Whether to inject this parameter
 */
internal data class InjectionInfo(
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
internal object ParameterInjector {
    /**
     * Finds indices of parameters that can be injected from context.
     *
     * @param parameters The list of constructor parameters to analyze
     * @return A map from injection type to parameter index
     */
    context(resolver: Resolver)
    internal fun findInjectionIndices(parameters: List<KSValueParameter>): Map<InjectionType, Int> {
        val indices = mutableMapOf<InjectionType, Int>()
        parameters.forEachIndexed { index, param ->
            val resolved = param.type.resolve()
            val declaration = resolved.declaration
            val qualifiedName = declaration.qualifiedName?.asString().orEmpty()
            val simpleName = declaration.simpleName.asString()
            when {
                resolved.isSubtypeOf<Environment<*, *>>() -> indices[InjectionType.ENVIRONMENT] = index
                resolved.isSubtypeOf<RandomGenerator>() -> indices[InjectionType.GENERATOR] = index
                resolved.isSubtypeOf<Incarnation<*, *>>() -> indices[InjectionType.INCARNATION] = index
                resolved.isSubtypeOf<Node<*>>() -> indices[InjectionType.NODE] = index
                resolved.isSubtypeOf<Reaction<*>>() -> indices[InjectionType.REACTION] = index
                resolved.isSubtypeOf<TimeDistribution<*>>() -> indices[InjectionType.TIMEDISTRIBUTION] = index
                isFilterType(resolved, simpleName, qualifiedName) -> indices[InjectionType.FILTER] = index
            }
        }
        return indices
    }

    context(resolver: Resolver)
    private inline fun <reified T> KSType.isSubtypeOf(): Boolean =
        checkNotNull(resolver.getClassDeclarationByName<T>()).asStarProjectedType().isAssignableFrom(this)

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
     * @return The determined context type
     */
    // Determine which context is active by checking manual overrides first, then injection mix.
    fun determineContextType(injectionIndices: Map<InjectionType, Int>): ContextType =
        determineContextTypeFromInjections(injectionIndices)

    private fun determineContextTypeFromInjections(injectionIndices: Map<InjectionType, Int>): ContextType = when {
        injectionIndices.containsKey(InjectionType.FILTER) -> ContextType.DEPLOYMENT_CONTEXT
        hasProgramContextInjections(injectionIndices) -> ContextType.PROGRAM_CONTEXT
        else -> determineDeploymentOrSimulationContext(injectionIndices)
    }

    private fun hasProgramContextInjections(injectionIndices: Map<InjectionType, Int>): Boolean {
        val hasNode = injectionIndices.containsKey(InjectionType.NODE)
        val hasReaction = injectionIndices.containsKey(InjectionType.REACTION)
        val hasTimeDistribution = injectionIndices.containsKey(InjectionType.TIMEDISTRIBUTION)
        return hasNode || hasReaction || hasTimeDistribution
    }

    private fun determineDeploymentOrSimulationContext(injectionIndices: Map<InjectionType, Int>): ContextType {
        val hasEnvironment = injectionIndices.containsKey(InjectionType.ENVIRONMENT)
        val hasGenerator = injectionIndices.containsKey(InjectionType.GENERATOR)
        val hasIncarnation = injectionIndices.containsKey(InjectionType.INCARNATION)
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
     * @param contextType The context type to determine which parameters can be injected
     * @return Set of parameter indices to skip
     */
    fun getInjectionParams(injectionIndices: Map<InjectionType, Int>, contextType: ContextType): Set<Int> {
        val paramsToSkip = mutableSetOf<Int>()
        if (isInjectionTypeAvailable(InjectionType.ENVIRONMENT, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.ENVIRONMENT,
                injectionIndices,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.GENERATOR, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.GENERATOR,
                injectionIndices,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.INCARNATION, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.INCARNATION,
                injectionIndices,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.NODE, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.NODE,
                injectionIndices,
                paramsToSkip,
            )
        }

        if (isInjectionTypeAvailable(InjectionType.REACTION, contextType)) {
            addInjectionParamIfEnabled(
                InjectionType.REACTION,
                injectionIndices,
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
        injectionIndices: Map<InjectionType, Int>,
        paramsToSkip: MutableSet<Int>,
    ) {
        if (injectionIndices.containsKey(injectionType)) {
            injectionIndices[injectionType]?.let { paramsToSkip.add(it) }
        }
    }
}
