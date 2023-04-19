/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.components

import it.unibo.alchemist.client.adapters.reactBootstrap.buttons.ToggleButton
import it.unibo.alchemist.client.adapters.reactBootstrap.buttons.ToggleButtonGroup
import it.unibo.alchemist.client.state.ClientStore.store
import it.unibo.alchemist.client.state.actions.SetRenderMode
import it.unibo.alchemist.common.model.RenderMode
import react.FC
import react.Props

/**
 * The button group that let the user decide which render mode to use.
 */
val RenderModeButtons: FC<Props> = FC {
    ToggleButtonGroup {
        name = "toggle-render-mode"
        onChange = { value ->
            store.dispatch(SetRenderMode(value as RenderMode))
        }
        defaultValue = RenderMode.AUTO
        ToggleButton {
            id = "client-button"
            value = RenderMode.CLIENT
            +"Client"
        }
        ToggleButton {
            id = "auto-button"
            value = RenderMode.AUTO
            +"Auto"
        }
        ToggleButton {
            id = "server-button"
            value = RenderMode.SERVER
            +"Server"
        }
    }
}
