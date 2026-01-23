/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts

import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation

val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
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
                val filter = RectangleFilter(-3.0, -3.0, 2.0, 2.0)
                val filter2 = RectangleFilter(3.0, 3.0, 2.0, 2.0)
                inside(filter) {
                    +testNodeProperty("a")
                }
                // otherwise
                inside(filter2) {
                    +testNodeProperty("b")
                }
            }
        }
    }
}

