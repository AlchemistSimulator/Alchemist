/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.utility

/**
 * Objects that store all the route path. Since this object is part of the common sourceSet, both Client and Serve can
 * refer to the correct Route maintaining consistency and reducing the number of possible errors and bug.
 */
object Routes {
    /**
     * Base environment path, for all Environment related operations.
     * Those are operations that acts directly on the original Enviornment interface.
     */
    private const val environmentPath: String = "/environment"

    /**
     * Route to get an Environment in a serialized form that needs to be rendered by the client, as
     * [it.unibo.alchemist.boundary.webui.common.model.RenderMode.CLIENT] was requested.
     */
    const val environmentClientPath: String = "$environmentPath/client"

    /**
     * Route to get an Environment already renderer by the Server, as
     * [it.unibo.alchemist.boundary.webui.common.model.RenderMode.SERVER] was requested.
     */
    const val environmentServerPath: String = "$environmentPath/server"

    /**
     * Base simulation path, for simulation related operations.
     * Those are operations that acts directly on the original Simulation interface.
     */
    private const val simulationPath: String = "/simulation"

    /**
     * Route to get the [it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate] of the simulation.
     */
    const val simulationStatusPath: String = "$simulationPath/status"

    /**
     * Route to start the simulation.
     */
    const val simulationPlayPath: String = "$simulationPath/play"

    /**
     * Route to pause the simulation.
     */
    const val simulationPausePath: String = "$simulationPath/pause"
}
