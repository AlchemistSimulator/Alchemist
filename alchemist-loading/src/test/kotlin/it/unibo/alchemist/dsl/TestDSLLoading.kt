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
import it.unibo.alchemist.boundary.exporters.CSVExporter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.extractors.moleculeReader
import it.unibo.alchemist.boundary.kotlindsl.ActionableContext
import it.unibo.alchemist.boundary.kotlindsl.contains
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.boundary.kotlindsl.simulationOnMap
import it.unibo.alchemist.boundary.variables.GeometricVariable
import it.unibo.alchemist.boundary.variables.LinearVariable
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.deployments.circle
import it.unibo.alchemist.model.deployments.grid
import it.unibo.alchemist.model.deployments.point
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
import org.apache.commons.math3.random.RandomGenerator
import org.junit.jupiter.api.Test

class TestDSLLoading {

    @Test
    fun `verify that the base syntax of the Kotlin DSL compiles and builds without throwing exceptions`() {
        simulation(ProtelisIncarnation<Euclidean2DPosition>()) {
            environment { }
        }.let { it.launcher.launch(it) }
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
                                -"token, 0, []"
                            }
                        }
                        program(
                            "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}",
                            rate,
                        )
                        program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
                    }
                }
            }
        }
        simulation(ProtelisIncarnation()) {
            exportWith(CSVExporter("test_export_interval", 4.0)) {
                -Time()
                -moleculeReader(
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
                            -"a"
                        }
                    }
                }
            }
        }
        simulation2D(SAPEREIncarnation()) {
            monitor(SimpleMonitor())
        }
        simulationOnMap(SAPEREIncarnation()) {
            environment(oSMEnvironment("vcm.pbf", false)) {
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
                                ActionableContext.action(
                                    reproduceGPSTrace(
                                        "gpsTrace",
                                        true,
                                        "AlignToSimulationTime",
                                    ),
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
                                -"token, 0, []"
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
                            ),
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
                                -token
                            }
                        }
                        program(
                            "{token} --> {firing}",
                            timeDistribution = 1,
                        )
                        program("{firing} --> +{token}")
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
                                -"token"
                            }
                        }
                    }
                }
            }
        }
        simulationOnMap(ProtelisIncarnation()) {
            environment(oSMEnvironment("vcm.pbf", false)) {
                terminator(StableForSteps(5, 100))
                deployments {
                    deploy(FromGPSTrace(7, "gpsTrace", true, "AlignToSimulationTime")) {
                        program(timeDistribution = 15) {
                            action(
                                reproduceGPSTrace(
                                    "gpsTrace",
                                    true,
                                    "AlignToSimulationTime",
                                ),
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
    companion object {
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
    }
}
