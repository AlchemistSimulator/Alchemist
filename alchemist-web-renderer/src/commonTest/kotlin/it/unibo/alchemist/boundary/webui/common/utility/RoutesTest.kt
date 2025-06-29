/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.utility

import kotlin.test.Test
import kotlin.test.assertEquals

class RoutesTest {

    @Test
    fun `all the routes strings should be correct`() {
        assertEquals("/environment/client", Routes.ENVIRONMENT_CLIENT_PATH)
        assertEquals("/environment/server", Routes.ENVIRONMENT_SERVER_PATH)
        assertEquals("/simulation/status", Routes.SIMULATION_STATUS_PATH)
        assertEquals("/simulation/play", Routes.SIMULATION_PLAY_PATH)
        assertEquals("/simulation/pause", Routes.SIMULATION_PAUSE_PATH)
    }
}
