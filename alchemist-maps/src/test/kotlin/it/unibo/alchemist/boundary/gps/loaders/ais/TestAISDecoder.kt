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
import kotlin.time.Instant

class TestAISDecoder :
    StringSpec({
        "AISDecoder should accept dates from resource names" {
            AISDecoder.parsePayload("\n\n", "ais-20260515.nmea") shouldBe emptyList()
        }

        "AISDecoder should fall back when no valid date is available" {
            AISDecoder.parsePayload("\n\n", "ais-without-date.nmea") shouldBe emptyList()
            AISDecoder.parsePayload("\n\n", "ais-20261340.nmea") shouldBe emptyList()
        }

        "AISDecoder should ignore blank payloads" {
            AISDecoder.parsePayload("\n\n", "2026-05-15") shouldBe emptyList()
        }

        "AISDecoder should accept the payload date as an instant" {
            AISDecoder.parsePayload("\n\n", Instant.parse("2026-05-15T00:00:00Z")) shouldBe emptyList()
        }
    })
