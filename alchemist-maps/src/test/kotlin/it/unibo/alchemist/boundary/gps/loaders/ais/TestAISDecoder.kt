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

class TestAISDecoder :
    StringSpec({
        "AISDecoder should extract dates from resource names" {
            AISDecoder.dateFrom("ais-20260515.nmea") shouldBe "2026-05-15"
        }

        "AISDecoder should fall back when no valid date is available" {
            AISDecoder.dateFrom("ais-without-date.nmea") shouldBe "1970-01-01"
            AISDecoder.dateFrom("ais-20261340.nmea") shouldBe "1970-01-01"
        }

        "AISDecoder should ignore blank payloads" {
            AISDecoder.parsePayload("\n\n", "2026-05-15") shouldBe emptyList()
        }
    })
