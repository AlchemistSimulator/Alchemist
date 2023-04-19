/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.model.serialization.jsonFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class StatusSurrogateTest : StringSpec({

    val listOfValues = listOf(
        StatusSurrogate.INIT,
        StatusSurrogate.READY,
        StatusSurrogate.PAUSED,
        StatusSurrogate.RUNNING,
        StatusSurrogate.TERMINATED,
    )

    "StatusSurrogate are just INIT, READY, PAUSED, RUNNING and TERMINATED" {
        val statuses = StatusSurrogate.values()
        statuses.size shouldBe 5
        statuses.map { it }.containsAll(listOfValues) shouldBe true
    }

    "StatusSurrogate should be serializable and deserializable" {
        listOfValues.forEach {
            val ser = jsonFormat.encodeToString(it)
            val des: StatusSurrogate = jsonFormat.decodeFromString(ser)
            it shouldBe des
        }
    }
})
