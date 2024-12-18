/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.state

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe

class CommonStateTest :
    StringSpec({
        "CommonState should be initialized with a default renderer" {
            val state = CommonState()
            state.renderer shouldNotBe null
        }
    })
