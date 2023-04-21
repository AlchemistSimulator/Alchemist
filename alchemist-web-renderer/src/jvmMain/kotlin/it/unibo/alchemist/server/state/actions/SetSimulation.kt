/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.state.actions

import it.unibo.alchemist.core.Simulation

/**
 * Redux action to set the [it.unibo.alchemist.core.interfaces.Simulation] of the application.
 * @param simulation the new [it.unibo.alchemist.core.interfaces.Simulation].
 */
data class SetSimulation(val simulation: Simulation<Any, Nothing>?)
