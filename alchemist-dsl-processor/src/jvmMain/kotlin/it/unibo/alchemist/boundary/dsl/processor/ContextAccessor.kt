package it.unibo.alchemist.boundary.dsl.processor

import it.unibo.alchemist.boundary.dsl.processor.data.InjectionType

/**
 * Provides accessor paths for injected parameters based on context type.
 */
internal object ContextAccessor {
    /**
     * Gets the accessor path for an injected parameter based on the context type.
     *
     * @param injectionType The type of injection
     * @param contextType The context type
     * @param contextParamName The name of the context parameter (default: "ctx")
     * @return The accessor path string
     */
    fun getAccessor(injectionType: InjectionType, contextType: ContextType, contextParamName: String = "ctx"): String =
        when (contextType) {
            ContextType.SIMULATION_CONTEXT -> getSimulationAccessor(injectionType, contextParamName)
            ContextType.EXPORTER_CONTEXT -> getExporterContextAccessor(injectionType, contextParamName)
            ContextType.GLOBAL_PROGRAMS_CONTEXT -> getGlobalProgramsContextAccessor(injectionType, contextParamName)
            ContextType.OUTPUT_MONITORS_CONTEXT -> getOutputMonitorsContextAccessor(injectionType, contextParamName)
            ContextType.TERMINATORS_CONTEXT -> getTerminatorsContextAccessor(injectionType, contextParamName)
            ContextType.DEPLOYMENTS_CONTEXT -> getDeploymentsContextAccessor(injectionType, contextParamName)
            ContextType.DEPLOYMENT_CONTEXT -> getDeploymentContextAccessor(injectionType, contextParamName)
            ContextType.PROGRAM_CONTEXT -> getProgramAccessor(injectionType, contextParamName)
            ContextType.PROPERTY_CONTEXT -> getPropertyAccessor(injectionType, contextParamName)
        }

    private fun getSimulationAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.environment"
            InjectionType.GENERATOR -> "$contextParamName.scenarioGenerator"
            InjectionType.INCARNATION -> "$contextParamName.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in SimulationContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in SimulationContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in SimulationContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in SimulationContext")
        }

    private fun getExporterContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.scenarioGenerator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in ExporterContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in ExporterContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in ExporterContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in ExporterContext")
        }

    private fun getGlobalProgramsContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.scenarioGenerator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in GlobalProgramsContext")
            InjectionType.REACTION -> throw IllegalArgumentException(
                "REACTION is not available in GlobalProgramsContext",
            )
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in GlobalProgramsContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in GlobalProgramsContext")
        }

    private fun getOutputMonitorsContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.scenarioGenerator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in OutputMonitorsContext")
            InjectionType.REACTION -> throw IllegalArgumentException(
                "REACTION is not available in OutputMonitorsContext",
            )
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in OutputMonitorsContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in OutputMonitorsContext")
        }

    private fun getTerminatorsContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.scenarioGenerator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in TerminatorsContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in TerminatorsContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in TerminatorsContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in TerminatorsContext")
        }

    private fun getDeploymentsContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in DeploymentsContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in DeploymentsContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in DeploymentsContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in DeploymentsContext")
        }

    private fun getDeploymentContextAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.ctx.incarnation"
            InjectionType.FILTER -> "$contextParamName.filter"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in DeploymentContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in DeploymentContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in DeploymentContext",
            )
        }

    private fun getProgramAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.ctx.ctx.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.ctx.ctx.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.ctx.ctx.ctx.incarnation"
            InjectionType.NODE -> "$contextParamName.node"
            InjectionType.REACTION -> "$contextParamName.reaction"
            InjectionType.TIMEDISTRIBUTION -> "$contextParamName.timeDistribution!!"
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in ProgramContext")
        }

    private fun getPropertyAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.ctx.ctx.ctx.environment"
            InjectionType.GENERATOR -> "$contextParamName.ctx.ctx.ctx.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.ctx.ctx.ctx.incarnation"
            InjectionType.NODE -> "$contextParamName.node"
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in PropertyContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in PropertyContext",
            )
            InjectionType.FILTER -> throw IllegalArgumentException("FILTER is not available in PropertyContext")
        }
}
