/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

/*
 * A gradient that follows its source, for trying the Compose UI by hand.
 * Every node computes its distance from the nearest node whose "source" is true, and stores it in "distance".
 * Nodes drift slowly, so the gradient keeps adapting.
 * Try it from the inspector: set "source" to true on a far node, or to false on the current one.
 */
import it.unibo.alchemist.boundary.composeui.ComposeMonitor
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
    monitor(ComposeMonitor())
    environment {
        networkModel(connectWithinDistance(0.8))
        deployments {
            deploy(grid(-5.0, -5.0, 5.0, 5.0, 0.5, 0.5, 0.2, 0.2)) {
                contents { "source"(position in Rectangle<Euclidean2DPosition>(-5.3, -5.3, 0.6, 0.6)) }
                program(gradient, 1.0)
                program("send")
                withTimeDistribution(1.0) {
                    program(genericReaction()) { action(brownianMove(0.02)) }
                }
            }
        }
    }
}
