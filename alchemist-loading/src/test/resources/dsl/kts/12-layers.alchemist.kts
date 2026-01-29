/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import it.unibo.alchemist.model.sapere.ILsaMolecule
val incarnation: ProtelisIncarnation<Euclidean2DPosition> = ProtelisIncarnation<Euclidean2DPosition>()
simulation(incarnation) {
    layer {
        molecule = "A"
        layer = StepLayer(
            2.0,
            2.0,
            100,
            0,
        )
    }
    layer {
        molecule = "B"
        layer = StepLayer(
            -2.0,
            -2.0,
            0,
            100,
        )
    }
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
            all {
                molecule = "a"
            }
        }
    }
}
