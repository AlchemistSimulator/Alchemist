package it.unibo.alchemist.boundary.dsl

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.model.DeploymentsContext
import it.unibo.alchemist.boundary.dsl.model.Incarnation as Inc
import it.unibo.alchemist.boundary.launchers.DefaultLauncher
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.SupportedIncarnations
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

class SimulationContext {
    var incarnation: Inc = Inc.SAPERE
    var environment: Environment<*, *> = defaultEnvironment()
    val ctxDeploy: DeploymentsContext = DeploymentsContext(this)

    @Suppress("UNCHECKED_CAST")
    private fun defaultEnvironment(): Environment<*, Euclidean2DPosition> {
        val inc = SupportedIncarnations.get<Any, Euclidean2DPosition>(incarnation.name).get()
        return Continuous2DEnvironment(inc)
    }

    fun deployments(block: DeploymentsContext.() -> Unit) {
        ctxDeploy.apply(block)
    }
}

fun createLoader(simBuilder: SimulationContext): Loader = object : DslLoader(simBuilder) {
    override val constants: Map<String, Any?> = emptyMap()
    override val dependentVariables: Map<String, DependentVariable<*>> = emptyMap()
    override val variables: Map<String, Variable<*>> = emptyMap()
    override val remoteDependencies: List<String> = emptyList()
    override val launcher: Launcher = DefaultLauncher()
}

fun simulation(block: SimulationContext.() -> Unit): Loader {
    val sim = SimulationContext().apply(block)
    return createLoader(sim)
}
