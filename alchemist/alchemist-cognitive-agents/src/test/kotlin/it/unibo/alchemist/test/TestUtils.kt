package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.loader.YamlLoader
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.kaikikm.threadresloader.ResourceLoader

/**
 * Run the simulation this environment owns.
 *
 * @param initialized
 *          the lambda to execute when the simulation begins.
 * @param stepDone
 *          the lambda to execute on each step of the simulation.
 * @param finished
 *          the lambda to execute at the end of the simulation.
 * @param numSteps
 *          the number of steps the simulation must execute.
 */
fun <T, P : Position<out P>> Environment<T, P>.startSimulation(
    initialized: (e: Environment<T, P>) -> Unit = { },
    stepDone: (e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) -> Unit = { _, _, _, _ -> Unit },
    finished: (e: Environment<T, P>, t: Time, s: Long) -> Unit = { _, _, _ -> Unit },
    numSteps: Long = 10000
): Environment<T, P> = Engine(this, numSteps).apply {
    addOutputMonitor(object : OutputMonitor<T, P> {
        override fun initialized(e: Environment<T, P>) = initialized.invoke(e)
        override fun stepDone(e: Environment<T, P>, r: Reaction<T>, t: Time, s: Long) = stepDone.invoke(e, r, t, s)
        override fun finished(e: Environment<T, P>, t: Time, s: Long) = finished.invoke(e, t, s)
    })
    play()
    run()
    error.ifPresent { throw it }
}.environment

/**
 * Loads a simulation from a YAML file.
 *
 * @param resource
 *          the name of the file containing the simulation to load.
 * @param vars
 *          a map specifying name-value bindings for the variables in this scenario.
 */
fun <T, P : Position<P>> loadYamlSimulation(resource: String, vars: Map<String, Double> = emptyMap()): Environment<T, P> =
    with(ResourceLoader.getResourceAsStream(resource)) {
        YamlLoader(this).getWith<T, P>(vars)
    }
