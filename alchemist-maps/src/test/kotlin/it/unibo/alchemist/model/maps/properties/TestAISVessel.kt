/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.maps.properties

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import it.unibo.alchemist.boundary.gps.loaders.ais.AISTraceLoader
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.maps.MapEnvironment
import it.unibo.alchemist.model.times.DoubleTime

class TestAISVessel :
    StringSpec({
        "AISVessel should expose current AIS data from its allocated trace" {
            val environment = environmentAt(0.0)
            val trace = AISTraceLoader(AIS_TRACE, NORMALIZER).first()
            val vessel = AISVessel(environment, node(), AIS_TRACE, true, NORMALIZER)
            vessel.vesselId shouldBe trace.vesselMMSI
            vessel.currentPayload shouldBe trace.payloadAt(DoubleTime(0.0))
            vessel.timestamp shouldBe trace.payloadAt(DoubleTime(0.0)).timestamp
            vessel.latitude shouldBe trace.payloadAt(DoubleTime(0.0)).latitude
            vessel.longitude shouldBe trace.payloadAt(DoubleTime(0.0)).longitude
        }

        "AISVessel should allocate MMSIs progressively in the same environment" {
            val environment = environmentAt(0.0)
            val expectedMmsis = AISTraceLoader(AIS_TRACE, NORMALIZER).map { it.vesselMMSI }
            val vessels = expectedMmsis.map { AISVessel(environment, node(), AIS_TRACE, true, NORMALIZER) }
            vessels.map(AISVessel<Any>::vesselId) shouldContainExactly expectedMmsis
        }

        "AISVessel should fail when more vessels than AIS traces are requested" {
            val environment = environmentAt(0.0)
            val traceCount = AISTraceLoader(AIS_TRACE, NORMALIZER).count()
            repeat(traceCount) {
                AISVessel(environment, node(), AIS_TRACE, true, NORMALIZER).vesselId
            }
            shouldThrow<IllegalStateException> {
                AISVessel(environment, node(), AIS_TRACE, true, NORMALIZER).vesselId
            }
        }
    })

private const val AIS_TRACE = "trace/ok/5min.nmea.txt"
private const val NORMALIZER = "AlignToSimulationTime"

private fun environmentAt(time: Double): MapEnvironment<Any, *, *> {
    val simulation = mockk<Simulation<Any, GeoPosition>>()
    every { simulation.time } returns DoubleTime(time)
    return mockk {
        every { simulationOrNull } returns simulation
    }
}

private fun node(): Node<Any> = mockk()
