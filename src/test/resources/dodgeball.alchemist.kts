import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation

/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
import it.unibo.alchemist.boundary.swingui.monitor.impl.SwingGUI
val incarnation = SAPERE.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
    networkModel = ConnectWithinDistance(0.5)
    monitors { +SwingGUI(environment)}
    deployments{
        deploy (grid(-5.0, -5.0, 5.0, 5.0,
            0.25, 0.25, 0.1, 0.1, 0.0, 0.0)){
            inside(RectangleFilter(-0.5, -0.5, 1.0, 1.0)) {
                molecule = "ball"
            }
            all{ molecule = "{hit, 0}"}
            programs {
                all{
                    timeDistribution("1")
                    program = "{ball} {hit, N} --> {hit, N + 1} {launching}"
                }
                all { program = "{launching} --> +{ball}"}
            }
        }
    }
}
