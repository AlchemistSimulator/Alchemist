package it.unibo.alchemist.boundary.dsl

import it.unibo.alchemist.boundary.DependentVariable
import it.unibo.alchemist.boundary.Launcher
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.dsl.model.Incarnation
import it.unibo.alchemist.boundary.launchers.DefaultLauncher

class SimulationContext {
    var incarnation: Incarnation = Incarnation.SAPERE
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
