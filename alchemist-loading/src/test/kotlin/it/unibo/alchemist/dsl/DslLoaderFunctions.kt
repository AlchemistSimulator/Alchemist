/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import another.location.SimpleMonitor
import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.properties.testNodeProperty
import it.unibo.alchemist.boundary.variables.GeometricVariable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.actions.brownianMove
import it.unibo.alchemist.model.deployments.Circle
import it.unibo.alchemist.model.deployments.Grid
import it.unibo.alchemist.model.deployments.Point
import it.unibo.alchemist.model.deployments.circle
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
import it.unibo.alchemist.model.deployments.polygon
import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.ProtelisIncarnation
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.layers.StepLayer
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.maps.actions.ReproduceGPSTrace
import it.unibo.alchemist.model.maps.deployments.FromGPSTrace
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
import it.unibo.alchemist.model.maps.environments.oSMEnvironment
import it.unibo.alchemist.model.nodes.testNode
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.sapere.ILsaMolecule
import it.unibo.alchemist.model.terminators.AfterTime
import it.unibo.alchemist.model.terminators.StableForSteps
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.WeibullTime
import it.unibo.alchemist.model.timedistributions.exponentialTime
import it.unibo.alchemist.model.timedistributions.weibullTime
import it.unibo.alchemist.model.times.DoubleTime
import it.unibo.alchemist.test.globalTestReaction
import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator

object DslLoaderFunctions {
    fun test01Nodes(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                deploy(point(0.0, 0.0))
                deploy(Point(ctx.environment, 0.0, 1.0))
            }
        }
    }

    fun test02ManyNodes(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            seeds {
                simulation(10L)
                scenario(20L)
            }
            networkModel = ConnectWithinDistance(0.5)
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
    fun test03Grid(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
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
    fun test05Content(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
            deployments {
                val hello = "hello"
                deploy(makePerturbedGridForTesting())
                {
                    all {
                        molecule = hello
                    }
                }
            }
        }
    }
    fun test06ContentFiltered(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
            deployments {
                val hello = "hello"
                deploy(makePerturbedGridForTesting()) {
                    all {
                        molecule = hello
                    }
                    inside(Rectangle(-1.0, -1.0, 2.0, 2.0)) {
                        molecule = "token"
                    }
                }
            }
        }
    }

    fun test07Programs(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
            deployments {
                val token = "token"
                deploy(makePerturbedGridForTesting()) {
                    inside(Rectangle(-0.5, -0.5, 1.0, 1.0)) {
                        molecule = token
                    }
                    programs {
                        all {
                            timeDistribution("1")
                            program = "{token} --> {firing}"
                        }
                        all {
                            program = "{firing} --> +{token}"
                        }
                    }
                }
            }
        }
    }
    fun test08ProtelisPrograms(): Loader = simulation(ProtelisIncarnation()) {
        deployments {
            deploy(point(1.5, 0.5)) {
                programs {
                    all {
                        timeDistribution = JaktaTimeDistribution(
                            sense = weibullTime(
                                1.0,
                                1.0,
                            ),
                            deliberate = DiracComb(0.1),
                            act = exponentialTime<Any>(1.0),
                        )
                        program = "1 + 1"
                    }
                }
            }
        }
    }
    fun test09TimeDistribution(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
            deployments {
                deploy(makePerturbedGridForTesting()) {
                    inside(Rectangle(-0.5, -0.5, 1.0, 1.0)) {
                        molecule = "token, 0, []"
                    }
                    programs {
                        all {
                            timeDistribution = DiracComb(0.5)
                            program = "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}"
                        }
                        all {
                            program = "{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}"
                        }
                    }
                }
            }
        }
    }
    fun test10Environment(): Loader {
        val incarnation = SAPEREIncarnation<GeoPosition>()
        val env = OSMEnvironment(incarnation, "vcm.pbf", false)
        return simulation(incarnation, { env }) {
            terminators {
                +StableForSteps<Any>(5, 100)
            }
            deployments {
                val gps = FromGPSTrace(
                    7,
                    "gpsTrace",
                    true,
                    "AlignToSimulationTime",
                )
                deploy(gps) {
                    programs {
                        all {
                            timeDistribution("15")
                            reaction = Event(node, timeDistribution)
                            addAction {
                                ReproduceGPSTrace(
                                    env,
                                    node,
                                    reaction,
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
    }
    fun test11monitors(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            monitors {
                +SimpleMonitor<List<ILsaMolecule>, Euclidean2DPosition>()
            }
        }
    }
    fun test12Layers(): Loader {
        val incarnation = ProtelisIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            layer {
                molecule = "A"
                layer = StepLayer(
                    2.0,
                    2.0,
                    100,
                    0,
                )
            }
            layer {
                molecule = "B"
                layer = StepLayer(
                    -2.0,
                    -2.0,
                    0,
                    100,
                )
            }
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
                    all {
                        molecule = "a"
                    }
                }
            }
        }
    }
    fun test13GlobalReaction(): Loader = simulation(ProtelisIncarnation()) {
        programs {
            +globalTestReaction(DiracComb(1.0))
        }
    }
    fun test14Exporters(): Loader = simulation(ProtelisIncarnation()) {
        exporter {
            type = CSVExporter(
                "test_export_interval",
                4.0,
            )
            data(
                Time(),
                moleculeReader(
                    "default_module:default_program",
                    null,
                    CommonFilters.NOFILTER.filteringPolicy,
                    emptyList(),
                ),
            )
        }
    }
    fun test15Variables(): Loader = simulation(SAPEREIncarnation()) {
        val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
        val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

        val mSize by variable { -size }
        val sourceStart by variable { mSize / 10.0 }
        val sourceSize by variable { size / 5.0 }
        terminators { +AfterTime<List<ILsaMolecule>, Euclidean2DPosition>(DoubleTime(1.0)) }
        networkModel = ConnectWithinDistance(0.5)
        deployments {
            deploy(makePerturbedGridForTesting()) {
                inside(Rectangle(sourceStart, sourceStart, sourceSize, sourceSize)) {
                    molecule = "token, 0, []"
                }
                programs {
                    all {
                        timeDistribution(rate.toString())
                        program = "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}"
                    }
                    all {
                        program = "{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}"
                    }
                }
            }
        }
    }

    fun test16ProgramsFilters(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(0.5)
            deployments {
                val token = "token"
                deploy(makePerturbedGridForTesting()) {
                    inside(Rectangle(-0.5, -0.5, 1.0, 1.0)) {
                        molecule = token
                    }
                    programs {
                        inside(Rectangle(-0.5, -0.5, 1.0, 1.0)) {
                            timeDistribution("1")
                            program = "{token} --> {firing}"
                        }
                        all {
                            program = "{firing} --> +{token}"
                        }
                    }
                }
            }
        }
    }
    fun test17CustomNodes(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        return simulation(incarnation) {
            deployments {
                deploy(
                    circle(
                        10,
                        0.0,
                        0.0,
                        5.0,
                    ),
                    nodeFactory = { testNode() },
                ) {
                }
            }
        }
    }
    fun test18NodeProperties(): Loader {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        return simulation(incarnation, { environment }) {
            deployments {
                deploy(
                    circle(
                        1000,
                        0.0,
                        0.0,
                        15.0,
                    ),
                ) {
                    properties {
                        val filter = Rectangle<Euclidean2DPosition>(-3.0, -3.0, 2.0, 2.0)
                        // same
                        inside(filter) {
                            +testNodeProperty("a")
                        }
                        inside(Rectangle(3.0, 3.0, 2.0, 2.0)) {
                            +testNodeProperty("b")
                        }
                    }
                }
            }
        }
    }
    fun test20Actions(): Loader = simulation(SAPEREIncarnation<GeoPosition>(), {
        oSMEnvironment()
    }) {
        networkModel = ConnectWithinDistance(1000.0)
        deployments {
            val lagoon = listOf(
                Pair(45.2038121, 12.2504425),
                Pair(45.2207426, 12.2641754),
                Pair(45.2381516, 12.2806549),
                Pair(45.2570053, 12.2895813),
                Pair(45.276336, 12.2957611),
                Pair(45.3029049, 12.2991943),
                Pair(45.3212544, 12.3046875),
                Pair(45.331875, 12.3040009),
                Pair(45.3453893, 12.3040009),
                Pair(45.3502151, 12.3156738),
                Pair(45.3622776, 12.3232269),
                Pair(45.3719259, 12.3300934),
                Pair(45.3830193, 12.3348999),
                Pair(45.395557, 12.3445129),
                Pair(45.3998964, 12.3300934),
                Pair(45.4018249, 12.3136139),
                Pair(45.4105023, 12.3122406),
                Pair(45.4167685, 12.311554),
                Pair(45.4278531, 12.3012543),
                Pair(45.4408627, 12.2902679),
                Pair(45.4355628, 12.2772217),
                Pair(45.4206242, 12.2703552),
                Pair(45.3994143, 12.2744751),
                Pair(45.3738553, 12.2676086),
                Pair(45.3579354, 12.2614288),
                Pair(45.3429763, 12.2497559),
                Pair(45.3198059, 12.2408295),
                Pair(45.2975921, 12.2346497),
                Pair(45.2802014, 12.2408295),
                Pair(45.257972, 12.233963),
                Pair(45.2038121, 12.2504425),
            )
            deploy(polygon(500, lagoon)) {
                programs {
                    all {
                        timeDistribution("10")
                        reaction = Event(node, timeDistribution)
                        addAction {
                            brownianMove(
                                0.0005,
                            )
                        }
                    }
                }
            }
        }
    }
}
