package it.unibo.alchemist.test

import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.core.implementations.Engine
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time

fun <T, P : Position<out P>> Environment<T, P>.startSimulation(
    initialized: (e: Environment<T, P>) -> Unit,
    stepDone: (e: Environment<T, P>, r: Actionable<T>?, t: Time, s: Long) -> Unit,
    finished: (e: Environment<T, P>, t: Time, s: Long) -> Unit,
) {
    with(Engine(this, Time.INFINITY)) {
        addOutputMonitor(
            object : OutputMonitor<T, P> {
                override fun initialized(environment: Environment<T, P>) = initialized.invoke(environment)
                override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, t: Time, s: Long) =
                    stepDone.invoke(environment, reaction, t, s)
                override fun finished(environment: Environment<T, P>, t: Time, s: Long) =
                    finished.invoke(environment, t, s)
            },
        )
        play()
        run()
        error.ifPresent { throw it }
    }
}

fun <T, P : Position<out P>> Environment<T, P>.startSimulationWithoutParameters(
    initialized: () -> Unit = { },
    stepDone: () -> Unit = { },
    finished: () -> Unit = { },
) = startSimulation(
    { initialized.invoke() },
    { _, _, _, _ -> stepDone.invoke() },
    { _, _, _ -> finished.invoke() },
)
