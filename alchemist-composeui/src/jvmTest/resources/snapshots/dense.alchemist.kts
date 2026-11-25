/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

// A dense random deployment: more than 20,000 links, so the UI samples them, for UI snapshots.
import it.unibo.alchemist.model.positionfilters.Rectangle
val gradient = """
    let source = env.has("source") && env.get("source");
    let distance = rep (d <- POSITIVE_INFINITY) {
      mux (source) { 0 } else { foldMin(POSITIVE_INFINITY, nbr(d) + self.nbrRange()) }
    };
    env.put("distance", distance);
    distance
""".trimIndent()

simulation2D(ProtelisIncarnation()) {
    scenarioSeed(0)
    simulationSeed(0)
    environment {
        networkModel(connectWithinDistance(1.0))
        deployments {
            deploy(circle(1500, 0.0, 0.0, 5.0)) {
                contents { "source"(position in Rectangle<Euclidean2DPosition>(-0.5, -0.5, 1.0, 1.0)) }
                program(gradient, 1.0)
                program("send")
            }
        }
    }
}
