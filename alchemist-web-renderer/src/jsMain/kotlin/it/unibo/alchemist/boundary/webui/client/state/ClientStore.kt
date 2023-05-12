/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.client.state

import org.reduxkotlin.Store
import org.reduxkotlin.createStore

/**
 * The store of the client.
 * This class uses the core concepts of the original Redux library.
 * Thanks to the singleton pattern, the [ClientState] can be accessed or edited from anywhere.
 * @see <a href="https://reduxkotlin.org/">ReduxKotlin Documentation</a>
 */
object ClientStore {

    /**
     * The redux store of the client.
     */
    val store: Store<ClientState> = createStore(::rootReducer, ClientState())
}
