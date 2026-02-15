/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package dsl.kts
import it.unibo.alchemist.boundary.kotlindsl.environment
import it.unibo.alchemist.boundary.kotlindsl.simulation2D

simulation2D(ProtelisIncarnation()) {
    environment {
        layer("A", StepLayer(2.0, 2.0, concentrationOf(100), concentrationOf(0)))
        layer("B", StepLayer(-2.0, -2.0, concentrationOf(0), concentrationOf(100)))
        deployments {
            deploy(grid(-5.0, -5.0, 5.0, 5.0, 0.25, 0.1, 0.1)) {
                contents {
                    -"a"
                }
            }
        }
    }
}
