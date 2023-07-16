/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.state.actions

import it.unibo.alchemist.core.Simulation

/**
 * Redux action to set the [Simulation] of the application.
 * @param simulation the new [Simulation].
 */
data class SetSimulation(val simulation: Simulation<Any, Nothing>?)
