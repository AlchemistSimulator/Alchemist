package it.unibo.alchemist.boundary.dsl.processor

/**
 * Configuration for the DSL processor, centralizing package names and type detection rules.
 * This makes the processor more maintainable and allows for easier customization.
 */
object ProcessorConfig {
    /**
     * Base package for Alchemist model classes.
     */
    const val MODEL_PACKAGE = "it.unibo.alchemist.model"

    /**
     * Base package for Alchemist boundary DSL classes.
     */
    const val DSL_PACKAGE = "it.unibo.alchemist.boundary.dsl"

    /**
     * Package where generated code will be placed.
     */
    const val GENERATED_PACKAGE = "$DSL_PACKAGE.generated"

    /**
     * Package for DSL model classes (contexts).
     */
    const val DSL_MODEL_PACKAGE = "$DSL_PACKAGE.model"

    /**
     * Fully qualified name for the Position interface.
     */
    const val POSITION_TYPE = "$MODEL_PACKAGE.Position"

    /**
     * Fully qualified name for the Environment interface.
     */
    const val ENVIRONMENT_TYPE = "$MODEL_PACKAGE.Environment"

    /**
     * Fully qualified name for the Incarnation interface.
     */
    const val INCARNATION_TYPE = "$MODEL_PACKAGE.Incarnation"

    /**
     * Fully qualified name for the Node class.
     */
    const val NODE_TYPE = "$MODEL_PACKAGE.Node"

    /**
     * Fully qualified name for the Reaction interface.
     */
    const val REACTION_TYPE = "$MODEL_PACKAGE.Reaction"

    /**
     * Fully qualified name for the TimeDistribution interface.
     */
    const val TIME_DISTRIBUTION_TYPE = "$MODEL_PACKAGE.TimeDistribution"

    /**
     * Fully qualified name for RandomGenerator.
     */
    const val RANDOM_GENERATOR_TYPE = "org.apache.commons.math3.random.RandomGenerator"

    /**
     * Package patterns that should be considered for Environment type detection.
     * This replaces the hardcoded "maps" check.
     */
    val ENVIRONMENT_PACKAGE_PATTERNS = setOf(
        MODEL_PACKAGE,
        "$MODEL_PACKAGE.maps",
        "$MODEL_PACKAGE.environments",
    )

    /**
     * Context type class names.
     */
    object ContextTypes {
        const val SIMULATION_CONTEXT = "$DSL_MODEL_PACKAGE.SimulationContext"
        const val DEPLOYMENTS_CONTEXT = "$DSL_MODEL_PACKAGE.DeploymentsContext"
        const val PROGRAMS_CONTEXT = "$DSL_MODEL_PACKAGE.ProgramsContext"
        const val PROPERTIES_CONTEXT = "$DSL_MODEL_PACKAGE.PropertiesContext"
    }

    /**
     * Checks if a qualified name matches any of the environment package patterns.
     *
     * @param qualifiedName The fully qualified name to check
     * @return True if the name matches an environment package pattern
     */
    fun isEnvironmentPackage(qualifiedName: String): Boolean = ENVIRONMENT_PACKAGE_PATTERNS.any { pattern ->
        qualifiedName.startsWith(pattern)
    }
}
