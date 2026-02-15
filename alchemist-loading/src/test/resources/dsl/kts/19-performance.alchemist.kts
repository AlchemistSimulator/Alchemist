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
import it.unibo.alchemist.boundary.extractors.Time
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.positionfilters.Rectangle
import kotlin.collections.emptyList

simulation2D(SAPEREIncarnation()) {
    simulationSeed(24L)
    scenarioSeed(42L)
    val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
    val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
    environment {
        networkModel(ConnectWithinDistance(0.5))
        val mSize = -size
        val sourceStart = mSize / 10.0
        val sourceSize = size / 5.0
        monitor(SimpleMonitor())
        deployments {
            deploy(circle(200, 0.0, 0.0, 20.0)) {
                contents {
                    - "basemolecule"
                    if (position in Rectangle<Euclidean2DPosition>(-5.0, -5.0, 10.0, 10.0)) {
                        - "centermolecule"
                    }
                    program("{basemolecule} --> {processed}", 1.0)
                    program("{processed} --> +{basemolecule}")                        }
            }
            deploy(grid(mSize, mSize, size, size, 0.25, 0.25, 0.1, 0.1)) {
                contents {
                    - "gridmolecule"
                    if (position in Rectangle<Euclidean2DPosition>(sourceStart, sourceStart, sourceSize, sourceSize)) {
                        -"token, 0, []"
                    }
                    if (position in Rectangle<Euclidean2DPosition>(-2.0, -2.0, 4.0, 4.0)) {
                        -"filteredmolecule"
                    }
                }
                program("{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}", rate)
                program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
                if (position in Rectangle<Euclidean2DPosition>(-1.0, -1.0, 2.0, 2.0)) {
                    program("{filteredmolecule} --> {active}", 0.5)
                }
            }
        }
    }
    exportWith(CSVExporter("performance_test", 1.0)) {
        -Time()
        -moleculeReader("token", null, CommonFilters.NOFILTER.filteringPolicy, emptyList())
    }
}
