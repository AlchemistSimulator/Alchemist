/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts

import it.unibo.alchemist.boundary.kotlindsl.TestDSLLoading.Companion.makePerturbedGridForTesting
import it.unibo.alchemist.boundary.kotlindsl.contains
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.positionfilters.Rectangle

simulation2D(SAPEREIncarnation()) {
    val rate: Double by variable(GeometricVariable(2.0, 0.1, 10.0, 9))
    val size: Double by variable(LinearVariable(5.0, 1.0, 10.0, 1.0))
    environment {
        val mSize = -size
        val sourceStart = mSize / 10.0
        val sourceSize = size / 5.0
        networkModel(ConnectWithinDistance(0.5))
        deployments {
            deploy(makePerturbedGridForTesting()) {
                if (position in Rectangle(sourceStart, sourceStart, sourceSize, sourceSize)) {
                    contents {
                        -"token, 0, []"
                    }
                }
                program("{token, N, L} --> {token, N, L} *{token, N+#D, L add [#NODE;]}", rate)
                program("{token, N, L}{token, def: N2>=N, L2} --> {token, N, L}")
            }
        }
    }
}
