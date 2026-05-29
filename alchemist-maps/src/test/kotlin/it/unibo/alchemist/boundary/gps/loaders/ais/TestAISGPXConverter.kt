/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.gps.loaders.ais

import io.jenetics.jpx.GPX
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.util.stream.Collectors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.toJavaInstant

class TestAISGPXConverter :
    StringSpec({
        "AISGPXConverter should write one anonymized GPX file per vessel" {
            val outputDirectory = Files.createTempDirectory("alchemist-ais-gpx-test")
            try {
                AISGPXConverter().write(
                    listOf(
                        payloadAt(vesselId = 1, seconds = 2),
                        payloadAt(vesselId = 2, seconds = 1),
                        payloadAt(vesselId = 1, seconds = 0),
                    ),
                    outputDirectory,
                ) { vesselId -> "vessel-$vesselId" }
                Files.exists(outputDirectory.resolve("vessel-1.gpx")) shouldBe true
                Files.exists(outputDirectory.resolve("vessel-2.gpx")) shouldBe true
                Files.exists(outputDirectory.resolve("1.gpx")) shouldBe false
                val exportedTimes = GPX
                    .read(outputDirectory.resolve("vessel-1.gpx"))
                    .tracks()
                    .flatMap { track -> track.segments() }
                    .flatMap { segment -> segment.points() }
                    .map { point -> point.time.orElseThrow() }
                    .collect(Collectors.toList())
                exportedTimes shouldBe listOf(EPOCH.toJavaInstant(), (EPOCH + 2.seconds).toJavaInstant())
            } finally {
                outputDirectory.toFile().deleteRecursively()
            }
        }
    })

private fun payloadAt(vesselId: Int, seconds: Long) = AISPayload(
    vesselMMSI = vesselId,
    timestamp = EPOCH + seconds.seconds,
    longitude = vesselId.toDouble(),
    latitude = vesselId.toDouble(),
)

private val EPOCH = Instant.fromEpochSeconds(0)
