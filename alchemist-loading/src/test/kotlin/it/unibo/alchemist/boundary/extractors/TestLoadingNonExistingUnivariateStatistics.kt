/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.model.SupportedIncarnations
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class TestLoadingNonExistingUnivariateStatistics {
    @Test
    fun testLoadingNonExistingUnivariateStatistics() {
        val incarnation =
            SupportedIncarnations
                .get<Any, Nothing>(SupportedIncarnations.getAvailableIncarnations().first())
                .orElseThrow()
        assertNotNull(incarnation)
        assertThrows<IllegalArgumentException> {
            MoleculeReader(
                moleculeName = "m",
                property = null,
                incarnation = incarnation,
                filter = CommonFilters.NOFILTER.filteringPolicy,
                aggregatorNames = listOf("non-existing-statistic"),
            )
        }
    }
}
