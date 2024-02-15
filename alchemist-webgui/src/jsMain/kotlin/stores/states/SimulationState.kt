/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.states

import it.unibo.alchemist.boundary.graphql.client.SimulationStatusQuery

/**
 * Represents the state of a simulation, including its status data.
 * @property status The status data of the simulation, represented by a SimulationStatusQuery.Data object. Defaults to null.
 * @constructor Creates a SimulationState with the specified status data, which defaults to null.
 */
data class SimulationState(val status: SimulationStatusQuery.Data? = null)
