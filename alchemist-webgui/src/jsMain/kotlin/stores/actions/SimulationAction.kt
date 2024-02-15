/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.actions

import io.kvision.redux.RAction
import it.unibo.alchemist.boundary.graphql.client.SimulationStatusQuery

/**
 * Represents actions related to simulation in the application.
 * This sealed class defines different types of actions as its subclasses.
 */
sealed class SimulationAction : RAction {

    /**
     * Action to set the simulation state.
     * @param simulationState The new simulation state to set.
     */
    data class SetSimulation(val simulationState: SimulationStatusQuery.Data?) : SimulationAction()
}
