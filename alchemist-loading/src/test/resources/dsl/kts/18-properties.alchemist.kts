/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts

import it.unibo.alchemist.model.positionfilters.Rectangle as InRectangle

simulation(SAPEREIncarnation<Euclidean2DPosition>()) {
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
                val filter = InRectangle<Euclidean2DPosition>(-3.0, -3.0, 2.0, 2.0)
                val filter2 = InRectangle<Euclidean2DPosition>(3.0, 3.0, 2.0, 2.0)
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

