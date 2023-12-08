/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.components

import it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.navbar.Navbar
import it.unibo.alchemist.boundary.webui.client.adapters.reactBootstrap.navbar.NavbarBrand
import it.unibo.alchemist.boundary.webui.client.state.ClientStore.store
import it.unibo.alchemist.boundary.webui.common.model.surrogate.StatusSurrogate
import react.FC
import react.Props
import react.useState

/**
 * The application Navbar.
 */
val AppNavbar: FC<Props> = FC {

    var statusSurrogate: StatusSurrogate by useState { StatusSurrogate.INIT }

    store.subscribe {
        statusSurrogate = store.state.statusSurrogate
    }

    Navbar {
        bg = "dark"
        variant = "dark"
        NavbarBrand {
            +"Alchemist Web Renderer"
        }
        RenderModeButtons()
        PlayButton {
            status = statusSurrogate
        }
    }
}
