/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.state

import kotlin.test.Test
import kotlin.test.assertNotNull

class CommonStateTest {

    @Test
    fun `common state should be initialized with a default renderer`() {
        val state = CommonState()
        assertNotNull(state.renderer, "CommonState.renderer should not be null")
    }
}
