/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.common.model

/**
 * Enum to represent the Render mode of the application.
 */
enum class RenderMode {
    /**
     * The rendering computation is executed on the client.
     */
    CLIENT,

    /**
     * A strategy will choose the best rendering mode.
     */
    AUTO,

    /**
     * The rendering computation is executed on the server.
     */
    SERVER,
}
