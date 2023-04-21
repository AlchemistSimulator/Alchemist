/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.surrogates.utility

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import it.unibo.alchemist.common.model.surrogate.StatusSurrogate
import it.unibo.alchemist.core.Status

class ToStatusSurrogateTest : StringSpec({
    "ToStatusSurrogate should map a Status to a StatusSurrogate" {
        Status.INIT.toStatusSurrogate() shouldBe StatusSurrogate.INIT
        Status.READY.toStatusSurrogate() shouldBe StatusSurrogate.READY
        Status.PAUSED.toStatusSurrogate() shouldBe StatusSurrogate.PAUSED
        Status.RUNNING.toStatusSurrogate() shouldBe StatusSurrogate.RUNNING
        Status.TERMINATED.toStatusSurrogate() shouldBe StatusSurrogate.TERMINATED
    }
})
