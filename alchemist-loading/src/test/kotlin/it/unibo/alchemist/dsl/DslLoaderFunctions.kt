/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import it.unibo.alchemist.boundary.Loader
import it.unibo.alchemist.boundary.dsl.model.Incarnation.PROTELIS
import it.unibo.alchemist.boundary.dsl.model.Incarnation.SAPERE
import it.unibo.alchemist.boundary.dsl.model.incarnation
import it.unibo.alchemist.boundary.dsl.model.simulation
import it.unibo.alchemist.jakta.timedistributions.JaktaTimeDistribution
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.deployments.Circle
import it.unibo.alchemist.model.deployments.Grid
import it.unibo.alchemist.model.deployments.Point
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.maps.actions.ReproduceGPSTrace
import it.unibo.alchemist.model.maps.deployments.FromGPSTrace
import it.unibo.alchemist.model.maps.environments.OSMEnvironment
import it.unibo.alchemist.model.positionfilters.Rectangle
import it.unibo.alchemist.model.reactions.Event
import it.unibo.alchemist.model.terminators.StableForSteps
import it.unibo.alchemist.model.timedistributions.DiracComb
import it.unibo.alchemist.model.timedistributions.ExponentialTime
import it.unibo.alchemist.model.timedistributions.WeibullTime
import org.apache.commons.math3.random.MersenneTwister

object DslLoaderFunctions {
    fun <T, P : Position<P>> test01Nodes(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                deploy(Point(environment, 0.0, 0.0))
                deploy(Point(environment, 0.0, 1.0))
            }
        }
    }

    fun <T, P : Position<P>> test02ManyNodes(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                generator = MersenneTwister(10)
                val circle = Circle(
                    environment,
                    generator,
                    1000,
                    0.0,
                    0.0,
                    10.0,
                )
                deploy(circle)
            }
        }
    }

    fun <T, P : Position<P>> test03Grid(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                val grid = Grid(
                    environment,
                    generator,
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

    fun <T, P : Position<P>> test05Content(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                val hello = "hello"
                deploy(
                    Grid(
                        environment, generator,
                        -5.0,
                        -5.0,
                        5.0,
                        5.0,
                        0.25, 0.25, 0.1, 0.1,
                    ),
                ) {
                    all {
                        molecule = hello
                    }
                }
            }
        }
    }

    fun <T, P : Position<P>> test06ContentFiltered(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                val hello = "hello"
                deploy(
                    Grid(
                        environment, generator,
                        -5.0,
                        -5.0,
                        5.0,
                        5.0,
                        0.25, 0.25, 0.1, 0.1,
                    ),
                ) {
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

    fun <T, P : Position<P>> test07Programs(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                val token = "token"
                deploy(
                    Grid(
                        environment, generator,
                        -5.0,
                        -5.0,
                        5.0,
                        5.0,
                        0.25, 0.25, 0.1, 0.1,
                    ),
                ) {
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

    fun <T, P : Position<P>> test08ProtelisPrograms(): Loader {
        val incarnation = PROTELIS.incarnation<T, P>()
        return simulation(incarnation) {
            deployments {
                deploy(Point(environment, 1.5, 0.5)) {
                    programs {
                        all {
                            timeDistribution = +JaktaTimeDistribution(
                                sense = WeibullTime(1.0, 1.0, generator),
                                deliberate = DiracComb(0.1),
                                act = ExponentialTime(1.0, generator),
                            )
                            program = "1 + 1"
                        }
                    }
                }
            }
        }
    }

    fun <T, P : Position<P>> test09TimeDistribution(): Loader {
        val incarnation = SAPERE.incarnation<T, P>()
        return simulation(incarnation) {
            networkModel = ConnectWithinDistance(5.0)
            deployments {
                deploy(
                    Grid(
                        environment, generator,
                        -5.0, -5.0, 5.0, 5.0, 0.25, 0.25, 0.1, 0.1,
                    ),
                ) {
                    inside(Rectangle(-0.5, -0.5, 1.0, 1.0)) {
                        molecule = "token, 0, []"
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
    }

    fun <T, P : Position<P>> test10Environment(): Loader {
        val incarnation = SAPERE.incarnation<T, GeoPosition>()
        val env = OSMEnvironment(incarnation, "vcm.pbf", false)
        return simulation(incarnation, env) {
            addTerminator(StableForSteps<Any>(5, 100))
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
                            timeDistribution("0.1")
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
}
