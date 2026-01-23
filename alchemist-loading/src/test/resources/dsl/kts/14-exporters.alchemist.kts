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

val incarnation = PROTELIS.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
    exporter {
        type = CSVExporter(
            "test_export_interval",
            4.0,
        )
        data(
            Time(),
            moleculeReader(
                "default_module:default_program",
                null,
                CommonFilters.NOFILTER.filteringPolicy,
                emptyList(),
            ),
        )
    }
}
