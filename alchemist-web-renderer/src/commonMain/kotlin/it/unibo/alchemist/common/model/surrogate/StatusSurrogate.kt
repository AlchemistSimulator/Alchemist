/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.common.model.surrogate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Multiplatform enum to use the [it.unibo.alchemist.core.interfaces.Status] outside a jvm.
 */
@SerialName("Status")
@Serializable
enum class StatusSurrogate {

    /**
     * The simulation is being initialized.
     */
    INIT,

    /**
     * The simulation is ready to be started.
     */
    READY,

    /**
     * The simulation is paused. It can be resumed.
     */
    PAUSED,

    /**
     * The simulation is currently running.
     */
    RUNNING,

    /**
     * The simulation is stopped. It is no longer possible to resume it.
     */
    TERMINATED,
}
