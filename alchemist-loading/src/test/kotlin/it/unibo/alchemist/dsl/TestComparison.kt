/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.dsl

import io.kotest.assertions.shouldFail
import it.unibo.alchemist.boundary.dsl.model.Incarnation
import it.unibo.alchemist.boundary.dsl.model.simulation
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.deployments.Circle
import it.unibo.alchemist.model.deployments.Grid
import it.unibo.alchemist.model.deployments.Point
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import org.apache.commons.math3.random.MersenneTwister
import org.junit.jupiter.api.Test

class TestComparison {

    @Test
    fun <T, P : Position<P>> test01Nodes() {
        val loader = simulation {
            incarnation = Incarnation.SAPERE
            environment(getDefault()) {
                networkModel = ConnectWithinDistance(5.0)
                deployments {
                    deploy(Point(environment, 0.0, 0.0))
                    deploy(Point(environment, 0.0, 1.0))
                }
            }
        }

        loader.shouldEqual("isac/01-nodes.yml")
    }

    @Test
    fun <T, P : Position<P>> test02ManyNodes() {
        val loader = simulation {
            incarnation = Incarnation.SAPERE
            environment(getDefault()) {
                networkModel = ConnectWithinDistance(5.0)
                deployments {
                    generator = MersenneTwister(10)
                    val cirle = Circle(
                        environment,
                        generator,
                        1000,
                        0.0,
                        0.0,
                        10.0,
                    )
                    deploy(cirle)
                }
            }
        }

        loader.shouldEqual("dsl/02-manynodes.yml")
    }

    @Test
    fun <T, P : Position<P>> testShouldFail() {
        val loader = simulation {
            incarnation = Incarnation.SAPERE
            environment(getDefault()) {
                networkModel = ConnectWithinDistance(5.0)
                deployments {
                    val grid = Grid(
                        environment,
                        generator,
                        -5.0,
                        -6.0, // different from the yml file
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
        shouldFail {
            loader.shouldEqual("dsl/03-grid.yml")
        }
    }

    @Test
    fun <T, P : Position<P>> test05Content() {
        val loader = simulation {
            incarnation = Incarnation.SAPERE
            environment(getDefault()) {
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
        loader.shouldEqual("dsl/05-content.yml")
    }
}
