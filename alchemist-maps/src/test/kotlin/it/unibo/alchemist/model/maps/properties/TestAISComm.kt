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
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import it.unibo.alchemist.boundary.gps.loaders.ais.AISPayload
import it.unibo.alchemist.model.Node
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TestAISComm :
    StringSpec({
        "AISComm should expose retained messages as a LIFO" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val comm = AISComm(node())

            comm.receive(first)
            comm.receive(second)

            comm.latestMessage shouldBe second
            comm.messages shouldContainExactly listOf(second, first)
        }

        "AISComm should expose navigation data from the latest message" {
            val comm = AISComm(node())

            comm.receive(payloadAt(0, speedOverGroundKnots = 10.0, cog = 90.0))

            comm.speedOverGroundKnots shouldBe 10.0
            comm.speedOverGroundMetersPerSecond shouldBe 5.144444444444445
            comm.courseOverGround shouldBe 90.0
        }

        "AISComm should trim retained messages to maxSize" {
            val first = payloadAt(0)
            val second = payloadAt(1)
            val third = payloadAt(2)
            val comm = AISComm(node(), maxSize = 2)

            listOf(first, second, third).forEach(comm::receive)

            comm.messages shouldContainExactly listOf(third, second)
        }

        "AISComm should discard messages outside the validity window" {
            val oldest = payloadAt(0)
            val withinWindow = payloadAt(3)
            val newest = payloadAt(5)
            val lateExpired = payloadAt(1)
            val comm = AISComm(node(), validityWindow = 3.seconds)

            listOf(oldest, withinWindow, newest, lateExpired).forEach(comm::receive)

            comm.messages shouldContainExactly listOf(newest, withinWindow)
        }

        "AISComm should clone retained messages and trimming policy" {
            val newest = payloadAt(2)
            val comm = AISComm(node(), maxSize = 1).apply {
                receive(payloadAt(1))
                receive(newest)
            }

            val clone = comm.cloneOnNewNode(node()) as AISComm<Any>
            clone.receive(payloadAt(3))

            clone.messages.size shouldBe 1
            clone.latestMessage shouldNotBe newest
        }

        "AISComm should reject invalid retention settings" {
            shouldThrow<IllegalArgumentException> { AISComm(node(), maxSize = 0) }
            shouldThrow<IllegalArgumentException> { AISComm(node(), validityWindow = (-1).seconds) }
        }
    })

private fun payloadAt(seconds: Long, speedOverGroundKnots: Double? = null, cog: Double? = null) = AISPayload(
    vesselId = seconds.toInt(),
    timestamp = EPOCH + seconds.seconds,
    longitude = seconds.toDouble(),
    latitude = seconds.toDouble(),
    speedOverGroundKnots = speedOverGroundKnots,
    cog = cog,
)

private val EPOCH = Instant.fromEpochSeconds(0)

private fun node(): Node<Any> = mockk()
