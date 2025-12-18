package it.unibo.alchemist.boundary.dsl.processor

/**
 * Configuration for the DSL processor, centralizing package names and type detection rules.
 * This makes the processor more maintainable and allows for easier customization.
 */
internal object ProcessorConfig {
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
     * Fully qualified name for PositionBasedFilter interface.
     */
    const val POSITION_BASED_FILTER_TYPE = "$MODEL_PACKAGE.PositionBasedFilter"

    /**
     * Context type class names.
     */
    object ContextTypes {
        /** Fully qualified name for SimulationContext. */
        const val SIMULATION_CONTEXT = "$DSL_MODEL_PACKAGE.SimulationContext"

        /** Fully qualified name for ExporterContext. */
        const val EXPORTER_CONTEXT = "$DSL_MODEL_PACKAGE.ExporterContext"

        /** Fully qualified name for GlobalProgramsContext. */
        const val GLOBAL_PROGRAMS_CONTEXT = "$DSL_MODEL_PACKAGE.GlobalProgramsContext"

        /** Fully qualified name for OutputMonitorsContext. */
        const val OUTPUT_MONITORS_CONTEXT = "$DSL_MODEL_PACKAGE.OutputMonitorsContext"

        /** Fully qualified name for TerminatorsContext. */
        const val TERMINATORS_CONTEXT = "$DSL_MODEL_PACKAGE.TerminatorsContext"

        /** Fully qualified name for DeploymentsContext. */
        const val DEPLOYMENTS_CONTEXT = "$DSL_MODEL_PACKAGE.DeploymentsContext"

        /** Fully qualified name for DeploymentContext. */
        const val DEPLOYMENT_CONTEXT = "$DSL_MODEL_PACKAGE.DeploymentContext"

        /** Fully qualified name for ProgramsContext. */
        const val PROGRAMS_CONTEXT = "$DSL_MODEL_PACKAGE.ProgramsContext"

        /** Fully qualified name for ProgramContext. */
        const val PROGRAM_CONTEXT = "$DSL_MODEL_PACKAGE.ProgramContext"

        /** Fully qualified name for PropertiesContext. */
        const val PROPERTIES_CONTEXT = "$DSL_MODEL_PACKAGE.PropertiesContext"

        /** Fully qualified name for PropertyContext. */
        const val PROPERTY_CONTEXT = "$DSL_MODEL_PACKAGE.PropertyContext"
    }
}
