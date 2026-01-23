/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts
import another.location.SimpleMonitor
import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation
import it.unibo.alchemist.boundary.extractors.Time
import org.apache.commons.math3.random.MersenneTwister

val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
val environment = {Continuous2DEnvironment(incarnation)}
simulation(incarnation, environment) {
    simulationGenerator = MersenneTwister(24L)
    scenarioGenerator = MersenneTwister(42L)

    networkModel = ConnectWithinDistance(0.5)

    val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
    val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))

    val mSize by variable { -size }
    val sourceStart by variable { mSize / 10.0 }
    val sourceSize by variable { size / 5.0 }

    layer {
        molecule = "A"
        layer = StepLayer(2.0, 2.0, 100.0, 0.0)
    }
    layer {
        molecule = "B"
        layer = StepLayer(-2.0, -2.0, 0.0, 100.0)
    }
    layer {
        molecule = "C"
        layer = StepLayer(0.0, 0.0, 50.0, 50.0)
    }

    monitors { +SimpleMonitor<Any, Euclidean2DPosition>()}

    exporter {
        type = CSVExporter(
            "performance_test",
            1.0,
        )
        data(
            Time(),
            moleculeReader(
                "token",
                null,
                CommonFilters.NOFILTER.filteringPolicy,
                emptyList(),
            ),
        )
    }

    deployments {
        deploy(
            circle(
                200,
                0.0,
                0.0,
                20.0,
            ),
        ) {
            all {
                molecule = "basemolecule"
            }
            inside(RectangleFilter(-5.0, -5.0, 10.0, 10.0)) {
                molecule = "centermolecule"
            }
            programs {
                all {
                    timeDistribution("1")
                    program = "{basemolecule} --> {processed}"
                }
                all {
                    program = "{processed} --> +{basemolecule}"
                }
            }
        }
        deploy(
            grid(
                mSize, mSize, size, size,
                0.25, 0.25, 0.1, 0.1,
            ),
        ) {
            all {
                molecule = "gridmolecule"
            }
            inside(RectangleFilter(sourceStart, sourceStart, sourceSize, sourceSize)) {
                molecule = "token, 0, []"
            }
            inside(RectangleFilter(-2.0, -2.0, 4.0, 4.0)) {
                molecule = "filteredmolecule"
            }
            programs {
                all {
                    timeDistribution(rate.toString())
                    program = "{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}"
                }
                all {
                    program = "{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}"
                }
                inside(RectangleFilter(-1.0, -1.0, 2.0, 2.0)) {
                    timeDistribution("0.5")
                    program = "{filteredmolecule} --> {active}"
                }
            }
        }
    }
}

