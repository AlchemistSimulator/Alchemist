/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package dsl.kts
import another.location.SimpleMonitor
import it.unibo.alchemist.boundary.kotlindsl.simulation2D

simulation2D(SAPEREIncarnation<Euclidean2DPosition>()) {
    monitor(SimpleMonitor())
}
