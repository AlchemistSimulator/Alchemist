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
import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.boundary.Variable
import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.variables.GeometricVariable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.dsl.DslLoaderFunctions
import it.unibo.alchemist.dsl.DslLoaderFunctions.makePerturbedGridForTesting
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Condition
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.GlobalReaction
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Layer
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.NodeProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.PositionBasedFilter
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.TerminationPredicate
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.deployments.circle
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.environments.continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.layers.StepLayer
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.maps.actions.reproduceGPSTrace
import it.unibo.alchemist.model.maps.deployments.FromGPSTrace
import it.unibo.alchemist.model.maps.environments.oSMEnvironment
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.event
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.terminators.StableForSteps
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.exponentialTime
import it.unibo.alchemist.model.timedistributions.weibullTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.test.globalTestReaction
import java.io.Serializable
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import org.apache.commons.math3.random.RandomGenerator

interface ActionableContext<T> {
    context(actionable: Actionable<T>)
    fun action(action: Action<T>) {
        actionable.actions += action
    }
    context(actionable: Actionable<T>)
    fun condition(condition: Condition<T>) {
        actionable.conditions += condition
    }
}

interface ContentContext<T> {

    context(incarnation: Incarnation<T, *>)
    fun concentrationOf(origin: Any?): T = incarnation.createConcentration(origin)

    context(node: Node<T>)
    operator fun Pair<Molecule, T>.unaryMinus() {
        node.setConcentration(first, second)
    }

    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun Molecule.unaryMinus() = -Pair(this, incarnation.createConcentration())

    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun String.unaryMinus() = -Pair(incarnation.createMolecule(this), incarnation.createConcentration())

    context(incarnation: Incarnation<T, *>, _: Node<T>)
    operator fun Pair<String, T>.unaryMinus() = -Pair(incarnation.createMolecule(first), second)

}

interface TimeDistributionContext<T, P : Position<P>> {

    context(timeDistribution: TimeDistribution<T>)
    val timeDistribution: TimeDistribution<T> get() = timeDistribution

    context(node: Node<T>)
    fun <R : Reaction<T>> program(reaction: R, block: context(R) ActionableContext<T>.() -> Unit = { }) {
        context(reaction) {
            object : ActionableContext<T> { }.block()
        }
        node.addReaction(reaction)
    }

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>, timeDistribution: TimeDistribution<T>)
    fun program(program: String?, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) =
        program(
            incarnation.createReaction(randomGenerator, environment, node, timeDistribution, program),
            block
        )
}

interface DeploymentContext<T, P : Position<P>> {

    val position: P

    fun <TimeDistributionType : TimeDistribution<T>> timeDistribution(
        timeDistribution: TimeDistributionType,
        block: context(TimeDistributionType) TimeDistributionContext<T, P>.() -> Unit
    ) {
        context(timeDistribution) {
            object : TimeDistributionContext<T, P> { }.block()
        }
    }

    @Suppress("UNCHECKED_CAST")
    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun withTimeDistribution(
        parameter: Any? = null,
        block: context(TimeDistribution<T>) TimeDistributionContext<T, P>.() -> Unit
    ) = timeDistribution<TimeDistribution<T>>(
        parameter as? TimeDistribution<T> ?: makeTimeDistribution(parameter),
        block
    )

    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
    fun program(program: String? = null, timeDistribution: Any? = null, block: context(Reaction<T>) ActionableContext<T>.() -> Unit = { }) =
        withTimeDistribution(timeDistribution) {
            program(program, block)
        }

    context(_: Incarnation<T, P>, _: Node<T>)
    fun contents(block: context(Incarnation<T, P>) ContentContext<T>.() -> Unit) {
        object : ContentContext<T> { }.block()
    }

    context(node: Node<T>)
    fun nodeProperty(property: NodeProperty<T>) {
        node.addProperty(property)
    }

    private companion object {

        context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>)
        fun <T, P : Position<P>> makeTimeDistribution(parameter: Any? = null): TimeDistribution<T> = incarnation.createTimeDistribution(
            randomGenerator,
            environment,
            node,
            parameter,
        )

        context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>, node: Node<T>, timeDistribution: TimeDistribution<T>)
        fun <T, P : Position<P>> makeReaction(descriptor: String?): Reaction<T> = incarnation.createReaction(
            randomGenerator,
            environment,
            node,
            timeDistribution,
            null,
        )
    }
}

interface DeploymentsContext<T, P : Position<P>> {
    context(incarnation: Incarnation<T, P>, randomGenerator: RandomGenerator, environment: Environment<T, P>)
    fun deploy(deployment: Deployment<P>, block: context(RandomGenerator, Node<T>) DeploymentContext<T, P>.() -> Unit = {}) {
        deployment.forEach { position ->
            val node: Node<T> = incarnation.createNode(randomGenerator, environment, TODO())
            context(node) {
                object : DeploymentContext<T, P> {
                    override val position: P get() = position
                }.block()
            }
            environment.addNode(node, position)
        }
    }
}

interface TerminatorsContext<T, P : Position<P>> {
    operator fun TerminationPredicate<T, P>.unaryMinus()
}

interface EnvironmentContext<T, P : Position<P>> {
    fun deployments(block: context(RandomGenerator) DeploymentsContext<T, P>.() -> Unit)
    fun globalProgram(timeDistribution: TimeDistribution<T>, globalReaction: GlobalReaction<T>, block: context(GlobalReaction<T>) ActionableContext<T>.() -> Unit = {})

    fun layer(molecule: Molecule, layer: Layer<T, P>)

    context(incarnation: Incarnation<T, P>)
    fun layer(molecule: String? = null, layer: Layer<T, P>) = layer(incarnation.createMolecule(molecule), layer)
    fun monitor(monitor: OutputMonitor<T, P>)
    fun networkModel(model: LinkingRule<T, P>)
    fun terminator(terminator: TerminationPredicate<T, P>)
}

interface ExporterContext<T, P : Position<P>> {
    operator fun Extractor<*>.unaryMinus()
}

interface VariableProvider<V : Serializable> {
    operator fun provideDelegate(
        thisRef: Any?,
        property: KProperty<*>,
    ): ReadOnlyProperty<Any?, V>
}

interface SimulationContext<T, P: Position<P>> {
    fun <E: Environment<T, P>> environment(environment: E, block: context(E) EnvironmentContext<T, P>.() -> Unit)
    fun exportWith(exporter: Exporter<T, P>, block: ExporterContext<T, P>.() -> Unit)
    fun scenarioRandomGenerator(randomGenerator: RandomGenerator)
    fun scenarioSeed(seed: Long)
    fun simulationRandomGenerator(randomGenerator: RandomGenerator)
    fun simulationSeed(seed: Long)

    fun <V : Serializable> variable(variable: Variable<out V>): VariableProvider<V>
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
        val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
        val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

        environment {
            val mSize = -size
            val sourceStart = mSize / 10.0
            val sourceSize = size / 5.0
            terminator(AfterTime(DoubleTime(1.0)))
            networkModel(ConnectWithinDistance(0.5))
            deployments {
                deploy(makePerturbedGridForTesting()) {
                    if (position in Rectangle(sourceStart, sourceStart, sourceSize, sourceSize)) {
                        contents {
                            - "token, 0, []"
                        }
                    }
                    program(
                        "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}",
                        rate
                    )
                    program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
                }
            }
        }
    }
    simulation(ProtelisIncarnation()) {
        exportWith(CSVExporter("test_export_interval", 4.0)) {
            - Time()
            - moleculeReader(
                "default_module:default_program",
                null,
                CommonFilters.NOFILTER.filteringPolicy,
                emptyList(),
            )
        }
    }
    simulation2D(ProtelisIncarnation()) {
        environment {
            globalProgram(DiracComb(1.0), globalTestReaction(DiracComb(1.0)))
        }
    }
    simulation2D(ProtelisIncarnation()) {
        environment {
            layer("A", StepLayer(2.0, 2.0, 100, 0))
            layer("B", StepLayer(-2.0, -2.0, 0, 100))
            deployments {
                deploy(
                    grid(
                        -5.0,
                        -5.0,
                        5.0,
                        5.0,
                        0.25,
                        0.1,
                        0.1,
                    ),
                ) {
                    contents {
                        - "a"
                    }
                }
            }
        }
    }
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
                    withTimeDistribution(15) {
                        program(event()) {
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
                    timeDistribution(DiracComb(0.5)) {
                        program("{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}")
                    }
                    program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
                }
            }
        }
    }
    simulation2D(ProtelisIncarnation()) {
        environment {
            deployments {
                deploy(point(1.5, 0.5)) {
                    timeDistribution(
                        JaktaTimeDistribution(
                            sense = weibullTime(1.0, 1.0),
                            deliberate = DiracComb(0.1),
                            act = exponentialTime<Any>(1.0),
                        )
                    ) {
                        program("1 + 1")
                    }
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
                    program(
                        "{token} --> {firing}",
                        timeDistribution = 1
                    )
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
            terminator(StableForSteps(5, 100))
            deployments {
                deploy(FromGPSTrace(7, "gpsTrace", true, "AlignToSimulationTime")) {
                    program(timeDistribution = 15) {
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
                deploy(makePerturbedGridForTesting()) {
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

