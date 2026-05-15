/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Instant

class TestAISPayload :
    StringSpec({
        "AISPayload should convert speed from knots to meters per second" {
            val payload = AISPayload(
                vesselId = 1,
                timestamp = Instant.EPOCH,
                longitude = 11.0,
                latitude = 44.0,
                speedOverGroundKnots = 10.0,
            )

            payload.speedOverGroundMetersPerSecond shouldBe 5.144444444444445
        }

        "AISPayload should keep missing speed unavailable" {
            val payload = AISPayload(
                vesselId = 1,
                timestamp = Instant.EPOCH,
                longitude = 11.0,
                latitude = 44.0,
            )

            payload.speedOverGroundMetersPerSecond shouldBe null
        }
    })
