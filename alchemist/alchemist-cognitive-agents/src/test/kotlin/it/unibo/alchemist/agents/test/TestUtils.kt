package it.unibo.alchemist.agents.test

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.kaikikm.threadresloader.ResourceLoader

fun <T, P : Position<out P>> Environment<T, P>.startSimulation(
    initialized: (e: Environment<T, P>) -> Unit,
    stepDone: (e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) -> Unit,
    finished: (e: Environment<T, P>, t: Time, s: Long) -> Unit,
    numSteps: Long = 10000
) {
    with(Engine(this, numSteps)) {
        addOutputMonitor(object : OutputMonitor<T, P> {
            override fun initialized(e: Environment<T, P>) = initialized.invoke(e)
            override fun stepDone(e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) = stepDone.invoke(e, r, t, s)
            override fun finished(e: Environment<T, P>, t: Time, s: Long) = finished.invoke(e, t, s)
        })
        play()
        run()
        error.ifPresent { throw it }
    }
}

fun <T, P : Position<out P>> Environment<T, P>.startSimulationWithoutParameters(
    initialized: () -> Unit = { },
    stepDone: () -> Unit = { },
    finished: () -> Unit = { },
    numSteps: Long = 10000
) = startSimulation(
        { initialized.invoke() },
        { _, _, _, _ -> stepDone.invoke() },
        { _, _, _ -> finished.invoke() },
        numSteps
)

fun <T, P : Position<P>> loadYamlSimulation(resource: String, vars: Map<String, Double> = emptyMap()) {
    val res = ResourceLoader.getResourceAsStream(resource)
    YamlLoader(res).getWith<T, P>(vars).startSimulationWithoutParameters()
}