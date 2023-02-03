/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.state

import org.reduxkotlin.Store
import org.reduxkotlin.threadsafe.createThreadSafeStore

/**
 * The store of the server.
 * This class uses the core concepts of the original Redux library.
 * Thanks to the singleton pattern, the [ServerState] can be accessed or edited from anywhere.
 * @see <a href="https://reduxkotlin.org/">ReduxKotlin Documentation</a>
 */
object ServerStore {
    /**
     * The redux store of the server.
     */
    val store: Store<ServerState> = createThreadSafeStore(::rootReducer, ServerState())
}
