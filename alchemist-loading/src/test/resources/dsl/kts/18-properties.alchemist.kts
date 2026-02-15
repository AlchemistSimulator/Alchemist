/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts

import it.unibo.alchemist.boundary.kotlindsl.contains
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D
import it.unibo.alchemist.model.positionfilters.Rectangle

simulation2D(SAPEREIncarnation()) {
    environment {
        deployments {
            deploy(circle(1000, 0.0, 0.0, 15.0)) {
                if (position in Rectangle(-3.0, -3.0, 2.0, 2.0)) {
                    nodeProperty(testNodeProperty("a"))
                }
                if (position in Rectangle(3.0, 3.0, 2.0, 2.0)) {
                    nodeProperty(testNodeProperty("b"))
                }
            }
        }
    }
}

