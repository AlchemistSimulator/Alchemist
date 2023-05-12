/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state.actions

import it.unibo.alchemist.boundary.webui.common.model.RenderMode

/**
 * Redux action to set the Render Mode of the application.
 * @param renderMode the new render mode.
 */
data class SetRenderMode(val renderMode: RenderMode)
