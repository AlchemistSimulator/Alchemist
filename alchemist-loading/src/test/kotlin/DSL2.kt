/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package attempt

import another.location.SimpleMonitor
import com.lowagie.tools.BuildTutorial.action
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.dsl.Dsl
import it.unibo.alchemist.dsl.DslLoaderFunctions
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.deployments.circle
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.environments.continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.maps.actions.reproduceGPSTrace
import it.unibo.alchemist.model.maps.deployments.FromGPSTrace
import it.unibo.alchemist.model.maps.environments.oSMEnvironment
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.event
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.terminators.StableForSteps
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.exponentialTime
import it.unibo.alchemist.model.timedistributions.weibullTime
import org.apache.commons.math3.random.RandomGenerator

interface ActionableContext<T> {
    fun action(action: Action<T>)
}

interface ContentContext<T> {

    context(incarnation: Incarnation<T, *>)
    fun concentrationOf(origin: Any?) = incarnation.createConcentration(origin)

    operator fun Pair<Molecule, T>.unaryMinus()

    context(incarnation: Incarnation<T, *>)
    operator fun Molecule.unaryMinus() = -Pair(this, incarnation.createConcentration())

    context(incarnation: Incarnation<T, *>)
    operator fun String.unaryMinus() = -Pair(incarnation.createMolecule(this), incarnation.createConcentration())

    context(incarnation: Incarnation<T, *>)
    operator fun Pair<String, T>.unaryMinus() = -Pair(incarnation.createMolecule(first), second)

}

interface DeploymentContext<T, P : Position<P>> {

    val position: P

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(timeDistribution: TimeDistribution<T>, actionable: Actionable<T>, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { })

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(
        timeDistribution: TimeDistribution<T>,
        actionable: context(TimeDistribution<T>) () -> Actionable<T> = { makeReaction(timeDistribution, null) },
        block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }
    ) = program(timeDistribution, context(timeDistribution) { actionable() }, block)

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(
        timeDistribution: Any? = null,
        actionable: context(TimeDistribution<T>) () -> Actionable<T> = { makeReaction(contextOf<TimeDistribution<T>>(), null) },
        block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }
    ) = program(makeTimeDistribution(timeDistribution), actionable, block)

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(timeDistribution: Any? = null, actionable: Actionable<T>, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) =
        program(makeTimeDistribution(timeDistribution), actionable, block)

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(timeDistribution: TimeDistribution<T>, descriptor: String? = null, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) =
        program(timeDistribution, makeReaction(timeDistribution, descriptor), block)

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(timeDistribution: Any? = null, descriptor: String? = null, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) {
        val timeDistribution = incarnation.createTimeDistribution(randomGenerator, environment, node, timeDistribution)
        program(timeDistribution, makeReaction(timeDistribution, descriptor), block)
    }

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(descriptor: String, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) =
        program(timeDistribution = null, descriptor, block)

    fun contents(block: context(Incarnation<T, P>) ContentContext<T>.() -> Unit)

    private companion object {

        context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
        fun <T, P : Position<P>> makeTimeDistribution(parameter: Any? = null) = incarnation.createTimeDistribution(
            randomGenerator,
            environment,
            node,
            parameter,
        )

        context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
        fun <T, P : Position<P>> makeReaction(timeDistribution: TimeDistribution<T>, descriptor: String?) = incarnation.createReaction(
            randomGenerator,
            environment,
            node,
            timeDistribution,
            null,
        )
    }
}

interface DeploymentsContext<T, P : Position<P>> {
    fun deploy(deployment: Deployment<P>, block: context(RandomGenerator, Node<T>, P) DeploymentContext<T, P>.() -> Unit = {})
}

interface TerminatorsContext<T, P : Position<P>> {
    operator fun TerminationPredicate<T, P>.unaryMinus()
    @Suppress("UNCHECKED_CAST")
    operator fun TerminationPredicate<T, *>.unaryMinus() = (this as TerminationPredicate<T, P>).unaryMinus()
    @Suppress("UNCHECKED_CAST")
    operator fun TerminationPredicate<*, P>.unaryMinus() = (this as TerminationPredicate<T, P>).unaryMinus()
    @Suppress("UNCHECKED_CAST")
    operator fun TerminationPredicate<*, *>.unaryMinus() = (this as TerminationPredicate<T, P>).unaryMinus()
}

interface EnvironmentContext<T, P : Position<P>> {
    fun deployments(block: context(RandomGenerator) DeploymentsContext<T, P>.() -> Unit)
    fun monitor(monitor: OutputMonitor<T, P>)
    fun networkModel(model: LinkingRule<T, P>)
    fun terminator(terminator: TerminationPredicate<T, P>)
}

interface SimulationContext<T, P: Position<P>> {
    fun <E: Environment<T, P>> environment(environment: E, block: context(E) EnvironmentContext<T, P>.() -> Unit)
    fun scenarioRandomGenerator(randomGenerator: RandomGenerator)
    fun scenarioSeed(seed: Long)
    fun simulationRandomGenerator(randomGenerator: RandomGenerator)
    fun simulationSeed(seed: Long)
}

operator fun PositionBasedFilter<*>.contains(position: Position<*>): Boolean = contains(position)

context(_: Incarnation<T, Euclidean2DPosition>)
fun <T> SimulationContext<T, Euclidean2DPosition>.environment(block: context(Continuous2DEnvironment<T>) EnvironmentContext<T, Euclidean2DPosition>.() -> Unit) =
    environment(continuous2DEnvironment(), block)

fun <T, P : Position<P>, I : Incarnation<T, P>> simulation(incarnation: I, block: context(I) SimulationContext<T, P>.() -> Unit): Unit = TODO()

fun <T, I : Incarnation<T, GeoPosition>> simulationOnMap(incarnation: I, block: context(I) SimulationContext<T, GeoPosition>.() -> Unit): Unit = simulation(incarnation, block)

fun <T, I : Incarnation<T, Euclidean2DPosition>> simulation2D(incarnation: I, block: context(I) SimulationContext<T, Euclidean2DPosition>.() -> Unit) = simulation(incarnation, block)

fun main() {
    simulation2D(SAPEREIncarnation()) {
        environment {
            monitor(SimpleMonitor())
        }
    }
    simulationOnMap(SAPEREIncarnation()){
        environment(oSMEnvironment( "vcm.pbf", false)) {
            terminator(StableForSteps(5, 100))
            deployments {
                val gps = FromGPSTrace(
                    7,
                    "gpsTrace",
                    true,
                    "AlignToSimulationTime",
                )
                deploy(gps) {
                    program(
                        timeDistribution = 15,
                        actionable =  { event() }
                    ) {
                        action(
                            reproduceGPSTrace(
                                "gpsTrace",
                                true,
                                "AlignToSimulationTime",
                            )
                        )
                    }
                }
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                deploy(makePerturbedGridForTesting()) {
                    if (position in Rectangle<Euclidean2DPosition>(-0.5, -0.5, 1.0, 1.0)) {
                        contents {
                            - "token, 0, []"
                        }
                    }
                    program(
                        DiracComb(0.5),
                            "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}"
                    )
                    program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
                }
            }
        }
    }
    simulation2D(ProtelisIncarnation()) {
        environment {
            deployments {
                deploy(point(1.5, 0.5)) {
                    program(
                        timeDistribution = JaktaTimeDistribution(
                            sense = weibullTime(1.0, 1.0),
                            deliberate = DiracComb(0.1),
                            act = exponentialTime<Any>(1.0),
                        ),
                        descriptor = "1 + 1"
                    )
                }
            }
        }
    }
    simulation2D(SAPEREIncarnation<Euclidean2DPosition>()) {
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                val token = "token"
                deploy(makePerturbedGridForTesting()) {
                    contents {
                        if (position in Rectangle<Euclidean2DPosition>(-0.5, -0.5, 1.0, 1.0)) {
                            - token
                        }
                    }
                    program(1, "{token} --> {firing}")
                    program( "{firing} --> +{token}")
                }
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                val hello = "hello"
                deploy(makePerturbedGridForTesting()) {
                    contents {
                        -hello
                        if (position in Rectangle<Euclidean2DPosition>(-1.0, -1.0, 2.0, 2.0)) {
                            - "token"
                        }
                    }
                }
            }
        }
    }
    simulationOnMap(ProtelisIncarnation()) {
        environment(oSMEnvironment( "vcm.pbf", false)) {
            terminators {
                -StableForSteps<Any>(5, 100)
            }
            deployments {
                deploy(FromGPSTrace(7, "gpsTrace", true, "AlignToSimulationTime")) {
                    program(timeDistribution = 15) {
                        action {
                            reproduceGPSTrace(
                                "gpsTrace",
                                true,
                                "AlignToSimulationTime",
                            )
                        }
                    }
                }
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            deployments {
                val p = point(0.0, 0.0)
                deploy(p)
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            networkModel(ConnectWithinDistance(5.0))
            deployments {
                deploy(point(0.0, 0.0))
                deploy(point(0.0, 1.0))
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        simulationSeed(10L)
        scenarioSeed(20L)
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                deploy(
                    circle(
                        10,
                        0.0,
                        0.0,
                        10.0,
                    ),
                )
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                val grid = grid(
                    -5.0,
                    -5.0,
                    5.0,
                    5.0,
                    0.25,
                    0.25,
                    0.0,
                    0.0,
                )
                deploy(grid)
            }
        }
    }
    simulation2D(SAPEREIncarnation()) {
        environment {
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                val hello = "hello"
                deploy(DslLoaderFunctions.makePerturbedGridForTesting()) {
                    contents {
                        -hello
                    }
                }
            }
        }
    }
}

context(_: RandomGenerator, _: Environment<*, Euclidean2DPosition>)
fun makePerturbedGridForTesting() = grid(
    -5.0,
    -5.0,
    5.0,
    5.0,
    0.25,
    0.25,
    0.1,
    0.1,
)

