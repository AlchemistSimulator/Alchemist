/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.sapere.timedistributions

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.incarnations.SAPEREIncarnation
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.sapere.nodes.LsaNode
import it.unibo.alchemist.model.times.DoubleTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import org.apache.commons.math3.random.Well19937c
import org.junit.jupiter.api.Test

class SAPERETimeDistributionTest {

    @Test
    fun `a fresh instance preserves the rate equation but not the generator object`() {
        val incarnation = SAPEREIncarnation<Euclidean2DPosition>()
        val environment = Continuous2DEnvironment(incarnation)
        val node = LsaNode(environment)
        val source = SAPEREExponentialTime("2", DoubleTime(3.0), Well19937c(0))

        val fresh = assertIs<SAPEREExponentialTime>(source.newInstanceOn(node))

        assertNotSame(source, fresh)
        assertEquals(2.0, fresh.rate)
        assertEquals(DoubleTime(3.0), fresh.startTime)
    }
}
