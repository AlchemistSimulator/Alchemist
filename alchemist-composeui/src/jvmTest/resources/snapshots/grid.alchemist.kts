/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

// A plain 2D grid computing a gradient from the bottom left corner, for UI snapshots.
import it.unibo.alchemist.model.positionfilters.Rectangle
val gradient = """
    let source = env.has("source") && env.get("source");
    let distance = rep (d <- POSITIVE_INFINITY) {
      mux (source) { 0 } else { minHood(nbr(d) + self.nbrRange()) }
    };
    env.put("distance", distance);
    distance
""".trimIndent()

simulation2D(ProtelisIncarnation()) {
    scenarioSeed(0)
    simulationSeed(0)
    environment {
        networkModel(connectWithinDistance(0.8))
        deployments {
            deploy(grid(-5.0, -5.0, 5.0, 5.0, 0.5, 0.5)) {
                contents { "source"(position in Rectangle<Euclidean2DPosition>(-5.3, -5.3, 0.6, 0.6)) }
                program(gradient, 1.0)
                program("send")
            }
        }
    }
}
