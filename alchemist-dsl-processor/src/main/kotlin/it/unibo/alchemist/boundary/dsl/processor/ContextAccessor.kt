package it.unibo.alchemist.boundary.dsl.processor

/**
 * Provides accessor paths for injected parameters based on context type.
 */
object ContextAccessor {
    /**
     * Gets the accessor path for an injected parameter based on the context type.
     *
     * @param injectionType The type of injection (ENVIRONMENT, GENERATOR, INCARNATION, NODE, REACTION, TIMEDISTRIBUTION)
     * @param contextType The context type (SIMULATION, DEPLOYMENT, PROGRAM, PROPERTY)
     * @param contextParamName The name of the context parameter (default: "ctx")
     * @return The accessor path string
     */
    fun getAccessor(injectionType: InjectionType, contextType: ContextType, contextParamName: String = "ctx"): String =
        when (contextType) {
            ContextType.SIMULATION -> getSimulationAccessor(injectionType, contextParamName)
            ContextType.DEPLOYMENT -> getDeploymentAccessor(injectionType, contextParamName)
            ContextType.PROGRAM -> getProgramAccessor(injectionType, contextParamName)
            ContextType.PROPERTY -> getPropertyAccessor(injectionType, contextParamName)
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
        }

    private fun getDeploymentAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.env"
            InjectionType.GENERATOR -> "$contextParamName.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.incarnation"
            InjectionType.NODE -> throw IllegalArgumentException("NODE is not available in DeploymentsContext")
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in DeploymentsContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in DeploymentsContext",
            )
        }

    private fun getProgramAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.ctx.env"
            InjectionType.GENERATOR -> "$contextParamName.ctx.ctx.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.ctx.ctx.incarnation"
            InjectionType.NODE -> "$contextParamName.node"
            InjectionType.REACTION -> "$contextParamName.reaction"
            InjectionType.TIMEDISTRIBUTION -> "$contextParamName.timeDistribution!!"
        }

    private fun getPropertyAccessor(injectionType: InjectionType, contextParamName: String): String =
        when (injectionType) {
            InjectionType.ENVIRONMENT -> "$contextParamName.ctx.ctx.env"
            InjectionType.GENERATOR -> "$contextParamName.ctx.ctx.generator"
            InjectionType.INCARNATION -> "$contextParamName.ctx.ctx.ctx.incarnation"
            InjectionType.NODE -> "$contextParamName.node"
            InjectionType.REACTION -> throw IllegalArgumentException("REACTION is not available in PropertyContext")
            InjectionType.TIMEDISTRIBUTION -> throw IllegalArgumentException(
                "TIMEDISTRIBUTION is not available in PropertyContext",
            )
        }
}
