/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK

class ResponseTest : StringSpec({
    "Responses code and content should be retrievable" {
        val response = Response(OK, "Good.")
        val intResponse = Response(Conflict, 1)
        val doubleOkResponde = Response(content = 5.6)
        response.code shouldBe OK
        response.content shouldBe "Good."
        intResponse.code shouldBe Conflict
        intResponse.content shouldBe 1
        doubleOkResponde.code shouldBe OK
        doubleOkResponde.content shouldBe 5.6
    }
})
